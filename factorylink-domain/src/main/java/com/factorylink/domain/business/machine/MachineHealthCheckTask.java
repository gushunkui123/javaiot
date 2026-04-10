package com.factorylink.domain.business.machine;

import com.factorylink.domain.business.machine.db.BizMachineEntity;
import com.factorylink.domain.business.machine.db.BizMachineService;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 设备在线状态健康检查定时任务
 * <p>
 * 每2分钟对所有启用的设备进行TCP端口探测，异步并行检测，
 * 将在线状态持久化到数据库。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MachineHealthCheckTask {

    private static final int TCP_CONNECT_TIMEOUT_MS = 3000;

    private final BizMachineService machineService;

    private final ExecutorService healthCheckExecutor = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors());

    /**
     * 每2分钟执行一次设备在线状态检测，可通过配置覆盖。
     */
    @Scheduled(fixedDelayString = "${factorylink.health-check.machine-interval:120000}")
    public void checkAllMachines() {
        List<BizMachineEntity> machines = machineService.listEnabled();
        if (machines.isEmpty()) {
            return;
        }

        log.debug("开始设备在线检测，共{}台", machines.size());

        // 并行检测所有设备
        List<CompletableFuture<Void>> futures = machines.stream()
            .map(machine -> CompletableFuture.runAsync(
                () -> checkSingleMachine(machine), healthCheckExecutor))
            .toList();

        // 等待所有检测完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        log.debug("设备在线检测完成");
    }

    private void checkSingleMachine(BizMachineEntity machine) {
        String ip = machine.getIp();
        int port = resolvePort(machine);

        if (ip == null || ip.isBlank() || port <= 0) {
            log.warn("设备[{}]缺少IP或端口配置，跳过检测", machine.getMachineName());
            return;
        }

        boolean online = tcpProbe(ip, port);
        try {
            machineService.updateOnlineStatus(machine.getMachineId(), online);
        } catch (Exception e) {
            log.warn("更新设备[{}]在线状态失败: {}", machine.getMachineName(), e.getMessage());
        }

        if (log.isDebugEnabled()) {
            log.debug("设备[{}] {}:{} → {}", machine.getMachineName(), ip, port,
                online ? "在线" : "离线");
        }
    }

    /**
     * 解析设备的探测端口：
     * 1. 优先使用 port 字段（贴标机）
     * 2. 其次从 apiUrl 中解析端口（磅秤设备）
     */
    private int resolvePort(BizMachineEntity machine) {
        if (machine.getPort() != null && machine.getPort() > 0) {
            return machine.getPort();
        }
        if (machine.getApiUrl() != null && !machine.getApiUrl().isBlank()) {
            try {
                int uriPort = URI.create(machine.getApiUrl()).getPort();
                if (uriPort > 0) {
                    return uriPort;
                }
            } catch (IllegalArgumentException e) {
                log.warn("设备[{}] apiUrl格式错误: {}", machine.getMachineName(), machine.getApiUrl());
            }
        }
        return -1;
    }

    /**
     * TCP 端口探测：尝试建立 TCP 连接，成功则在线，超时/拒绝则离线。
     */
    private boolean tcpProbe(String ip, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, port), TCP_CONNECT_TIMEOUT_MS);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

}
