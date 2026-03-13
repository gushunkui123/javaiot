# 微量磅秤系统数据 Http API 接口说明

## 1、接口 URL 定义

- 查询数据默认接口 URL 为 `http://192.168.99.202:8000/aws/api`
- 写入数据默认接口 URL 为 `http://192.168.99.202:8000/aws/api/wr`

> IP 地址最终以实际地址为准，以下内容均以此 url 举例

---

## 2、查询工单数据

### 2.1 请求说明

**请求地址:**

```
http://192.168.99.202:8000/aws/api?option=getWorkOrder&plant=PLANT&machineId=MACHINEID&workOrderNo=WORKORDERNO&formulaCode=FORMULACODE&orderDateFrom=ORDERDATEFROM&orderDateTo=ORDERDATETO&orderState=ORDERSTATE
```

**请求方法:** HTTP GET

**请求参数说明:**

| 参数 | 说明 |
|---|---|
| option | API 标识, 此请求固定为 `getWorkOrder` |
| plant | 工厂别，可为空 |
| machineId | 设备编号，如：1 或 2 等，不可为空；现场每台设备都有唯一编号，通过此栏位控制接口操作哪台设备数据 |
| workOrderNo | 工单编号，可为空 |
| formulaCode | 配方编号，可为空 |
| orderDateFrom | 起始日期，可为空，格式：yyyyMMdd，如 20190301 |
| orderDateTo | 截止日期，可为空，格式：yyyyMMdd，如 20190302 |
| orderState | 工单状态，可为空，1:未生产 2:生产中 3:已完工。如查询多个状态，中间用","分隔，如: 1,2 |

### 2.2 返回结果

**结果数据格式:** JSON

查询正确时返回结果示例如下：

```json
{
  "success": 1,
  "rtnmsg": "ok",
  "rtndata": [
    {
      "WorkOrderNo": "A11180211001",
      "OrderDate": "2018-02-11",
      "FormulaCode": "Test001",
      "OrderBatchNum": 1,
      "OrderWeight": 0.2000,
      "FinishBatchNum": 0,
      "FinishWeight": 0.0000,
      "StartTime": "",
      "FinishTime": "",
      "OrderState": 1
    },
    {
      "WorkOrderNo": "A11180113001",
      "OrderDate": "2018-01-13",
      "FormulaCode": "Test002",
      "OrderBatchNum": 3,
      "OrderWeight": 0.8970,
      "FinishBatchNum": 3,
      "FinishWeight": 0.8975,
      "StartTime": "2018-01-13 08:19:23",
      "FinishTime": "2018-01-13 08:20:02",
      "OrderState": 3
    }
  ]
}
```

查询出错时返回结果示例如下：

```json
{
  "success": 0,
  "rtnmsg": "Invalid parameter:option",
  "rtndata": []
}
```

**结果参数说明:**

| 参数 | 说明 |
|---|---|
| success | 结果是否成功 1:成功 0:失败 |
| rtnmsg | 结果内容 |
| WorkOrderNo | 工单编号 |
| OrderDate | 工单日期 |
| FormulaCode | 配方编号 |
| OrderBatchNum | 批次数 |
| OrderWeight | 工单总重 |
| FinishBatchNum | 完工批次数 |
| FinishWeight | 完工重量 |
| StartTime | 生产开始时间 |
| FinishTime | 生产结束时间 |
| OrderState | 工单状态，1:未生产 2:生产中 3:已完工 |

---

## 3、查询称量记录

### 3.1 请求说明

**请求地址:**

```
http://192.168.99.202:8000/aws/api?option=getWeighingRecord&plant=PLANT&machineId=MACHINEID&workOrderNo=WORKORDERNO&formulaCode=FORMULACODE&weighTimeFrom=WEIGHTIMEFROM&weighTimeTo=WEIGHTIMETO
```

**请求方法:** HTTP GET

**请求参数说明:**

| 参数 | 说明 |
|---|---|
| option | API 标识, 此请求固定为 `getWeighingRecord` |
| plant | 工厂别，可为空 |
| machineId | 设备编号，如：1 或 2 等，不可为空 |
| workOrderNo | 工单编号，可为空 |
| formulaCode | 配方编号，可为空 |
| weighTimeFrom | 起始时间，不可为空，格式：yyyyMMddHHmmss，如 20190302152000 |
| weighTimeTo | 截止时间，不可为空，格式：yyyyMMddHHmmss，如 20190302173000 |

### 3.2 返回结果

**结果数据格式:** JSON

查询正确时返回结果示例如下：

```json
{
  "success": 1,
  "rtnmsg": "ok",
  "rtndata": [
    {
      "WorkOrderNo": "A11180110001",
      "FormulaCode": "Test001",
      "Batch": 1,
      "MaterialNo": "A01",
      "WeighingValue": 0.0,
      "WeighTime": "2018-01-10 11:28:00",
      "IsError": 0
    },
    {
      "WorkOrderNo": "A11180110001",
      "FormulaCode": "Test001",
      "Batch": 1,
      "MaterialNo": "A03",
      "WeighingValue": 0.0,
      "WeighTime": "2018-01-10 11:28:00",
      "IsError": 0
    }
  ]
}
```

查询出错时返回结果示例如下：

```json
{
  "success": 0,
  "rtnmsg": "Invalid parameter:option",
  "rtndata": []
}
```

**结果参数说明:**

| 参数 | 说明 |
|---|---|
| success | 结果是否成功 1:成功 0:失败 |
| rtnmsg | 结果内容 |
| WorkOrderNo | 工单编号 |
| FormulaCode | 配方编号 |
| Batch | 批次 |
| MaterialNo | 原料编号 |
| WeighingValue | 计量重量, 单位:kg |
| WeighTime | 计量时间 |
| IsError | 计量是否出错, 0:未出错 1:出错 |

---

## 4、查询报警记录

### 4.1 请求说明

**请求地址:**

```
http://192.168.99.202:8000/aws/api?option=getAlarmLog&plant=PLANT&machineId=MACHINEID&alarmGroup=ALARMGROUP&alarmTimeFrom=ALARMTIMEFROM&alarmTimeTo=ALARMTIMETO
```

**请求方法:** HTTP GET

**请求参数说明:**

| 参数 | 说明 |
|---|---|
| option | API 标识, 此请求固定为 `getAlarmLog` |
| plant | 工厂别，可为空 |
| machineId | 设备编号，如：1 或 2 等，不可为空 |
| alarmGroup | 报警类别，可为空。E1-磅秤计量异常,E2-硬件/连线异常,E3-投料入桶异常 |
| alarmTimeFrom | 报警起始时间，不可为空，格式：yyyyMMddHHmmss，如 20190302152000 |
| alarmTimeTo | 报警截止时间，不可为空，格式：yyyyMMddHHmmss，如 20190302173000 |

### 4.2 返回结果

**结果数据格式:** JSON

查询正确时返回结果示例如下：

```json
{
  "success": 1,
  "rtnmsg": "ok",
  "rtndata": [
    {
      "AlarmGroup": "E1",
      "AlarmGroupText": "磅秤计量异常",
      "AlarmCode": "70",
      "AlarmMsg": "NO.01 桶低料位",
      "AlarmTime": "2018-12-21 10:36:47",
      "WorkOrderNo": "",
      "Batch": 0
    },
    {
      "AlarmGroup": "E1",
      "AlarmGroupText": "磅秤计量异常",
      "AlarmCode": "70",
      "AlarmMsg": "NO.02 桶低料位",
      "AlarmTime": "2018-12-21 10:36:47",
      "WorkOrderNo": "",
      "Batch": 0
    }
  ]
}
```

查询出错时返回结果示例如下：

```json
{
  "success": 0,
  "rtnmsg": "Invalid parameter:option",
  "rtndata": []
}
```

**结果参数说明:**

| 参数 | 说明 |
|---|---|
| success | 结果是否成功 1:成功 0:失败 |
| rtnmsg | 结果内容 |
| AlarmGroup | 报警分组编号 |
| AlarmGroupText | 报警分组 |
| AlarmCode | 报警内容编号 |
| AlarmMsg | 报警内容 |
| AlarmTime | 报警发生时间 |
| WorkOrderNo | 对应工单编号 |
| Batch | 对应批次 |

---

## 5、依条码追溯生产记录

### 5.1 请求说明

**请求地址:**

```
http://192.168.99.202:8000/aws/api?option=getTraceBackData&plant=PLANT&machineId=MACHINEID&barcode=BARCODE
```

**请求方法:** HTTP GET

**请求参数说明:**

| 参数 | 说明 |
|---|---|
| option | API 标识, 此请求固定为 `getTraceBackData` |
| plant | 工厂别，可为空 |
| machineId | 设备编号，如：1 或 2 等，不可为空 |
| barcode | 完工条码编号，不可为空 |

### 5.2 返回结果

**结果数据格式:** JSON

查询正确时返回结果示例如下：

```json
{
  "success": 1,
  "rtnmsg": "ok",
  "rtndata": [
    {
      "WorkOrderNo": "A11180110006",
      "Batch": 1,
      "BatchWeight": 0.1705,
      "Barcode": "A1118011000610001",
      "MaterialNo": "A01",
      "WeighingValue": 0.0805,
      "MaterialBarcode": "A0101",
      "LotNo": "180105P03",
      "WeighTime": "2018-01-10 13:32:39"
    },
    {
      "WorkOrderNo": "A11180110006",
      "Batch": 1,
      "BatchWeight": 0.1705,
      "Barcode": "A1118011000610001",
      "MaterialNo": "A02",
      "WeighingValue": 0.0900,
      "MaterialBarcode": "",
      "LotNo": "",
      "WeighTime": "2018-01-10 13:32:39"
    }
  ]
}
```

查询出错时返回结果示例如下：

```json
{
  "success": 0,
  "rtnmsg": "Invalid parameter:option",
  "rtndata": []
}
```

**结果参数说明:**

| 参数 | 说明 |
|---|---|
| success | 结果是否成功 1:成功 0:失败 |
| rtnmsg | 结果内容 |
| WorkOrderNo | 工单编号 |
| Batch | 批次 |
| BatchWeight | 批次重量 |
| Barcode | 批次条码编号 |
| MaterialNo | 原料编号 |
| WeighingValue | 称量重量(kg) |
| MaterialBarcode | 原料条码编号 |
| LotNo | 原料生产批号 |
| WeighTime | 称量时间 |

---

## 6、查询原料耗用

### 6.1 请求说明

**请求地址:**

```
http://192.168.99.202:8000/aws/api?option=getMaterialConsumption&plant=PLANT&machineId=MACHINEID&workOrderNo=WORKORDERNO&materialNo=MATERIALNO&consTimeFrom=CONSTIMEFROM&consTimeTo=CONSTIMETO
```

**请求方法:** HTTP GET

**请求参数说明:**

| 参数 | 说明 |
|---|---|
| option | API 标识, 此请求固定为 `getMaterialConsumption` |
| plant | 工厂别，可为空 |
| machineId | 设备编号，如：1 或 2 等，不可为空 |
| workOrderNo | 工单编号，可为空 |
| materialNo | 原料编号，可为空 |
| consTimeFrom | 起始日期，不可为空，格式：yyyyMMdd，如 20190301 |
| consTimeTo | 截止日期，不可为空，格式：yyyyMMdd，如 20190302 |

### 6.2 返回结果

**结果数据格式:** JSON

查询正确时返回结果示例如下：

```json
{
  "success": 1,
  "rtnmsg": "ok",
  "rtndata": [
    {
      "ConsumptionTime": "2018-1-10",
      "MaterialNo": "A01-A01-1",
      "WeighingValue": 0.0115
    },
    {
      "ConsumptionTime": "2018-1-10",
      "MaterialNo": "A02-A02-1",
      "WeighingValue": 0.0205
    }
  ]
}
```

查询出错时返回结果示例如下：

```json
{
  "success": 0,
  "rtnmsg": "Invalid parameter:option",
  "rtndata": []
}
```

**结果参数说明:**

| 参数 | 说明 |
|---|---|
| success | 结果是否成功 1:成功 0:失败 |
| rtnmsg | 结果内容 |
| ConsumptionTime | 耗用日期 |
| MaterialNo | 原料编号/名称 |
| WeighingValue | 耗用重量(kg) |

---

## 7、写入原料基本资料

### 7.1 新增原料基本资料

**写入参数说明:**

| 参数 | 数据类型 | 说明 |
|---|---|---|
| option | varchar(50) | API 标识, 此操作固定为 `addParts` |
| plant | varchar(50) | 工厂别，可为空 |
| machineId | int | 设备编号，如：1 或 2 等，不可为空 |
| partNo | varchar(50) | 原料编号，不可为空（只能包含字母、数字、中线和下划线且必须以字母或数字开头） |
| partName | varchar(100) | 原料名称，不可为空 |
| partClass | varchar(1) | 原料类别，可为空 |
| unitBarcodeWeight | decimal | 单位条码重量(kg)，可为空 |

**写入方法:** HTTP POST
**数据格式:** JSON

写入数据示例如下:

```json
{
  "option": "addParts",
  "plant": "",
  "machineId": 1,
  "partNo": "ZNO01",
  "partName": "氧化锌 01",
  "partClass": "",
  "unitBarcodeWeight": 25.0
}
```

写入成功返回结果:

```json
{"success": 1, "rtnmsg": "ok"}
```

写入失败返回结果示例:

```json
{"success": 0, "rtnmsg": "Invalid parameter:machineId"}
```

### 7.2 修改原料基本资料

**写入参数说明:**

| 参数 | 数据类型 | 说明 |
|---|---|---|
| option | varchar(50) | API 标识, 此操作固定为 `updateParts` |
| plant | varchar(50) | 工厂别，可为空 |
| machineId | int | 设备编号，如：1 或 2 等，不可为空 |
| partNo | varchar(50) | 原料编号，不可为空 |
| partName | varchar(100) | 原料名称，不可为空 |
| partClass | varchar(1) | 原料类别，可为空 |
| unitBarcodeWeight | decimal | 单位条码重量(kg)，可为空 |

**写入方法:** HTTP POST
**数据格式:** JSON

写入数据示例如下:

```json
{
  "option": "updateParts",
  "plant": "",
  "machineId": 1,
  "partNo": "ZNO01",
  "partName": "氧化锌 01",
  "partClass": "",
  "unitBarcodeWeight": 50.0
}
```

写入成功返回结果:

```json
{"success": 1, "rtnmsg": "ok"}
```

写入失败返回结果示例:

```json
{"success": 0, "rtnmsg": "Invalid parameter:machineId"}
```

### 7.3 删除原料基本资料

**写入参数说明:**

| 参数 | 数据类型 | 说明 |
|---|---|---|
| option | varchar(50) | API 标识, 此操作固定为 `deleteParts` |
| plant | varchar(50) | 工厂别，可为空 |
| machineId | int | 设备编号，如：1 或 2 等，不可为空 |
| partNo | varchar(50) | 原料编号，不可为空 |

**写入方法:** HTTP POST
**数据格式:** JSON

写入数据示例如下:

```json
{
  "option": "deleteParts",
  "plant": "",
  "machineId": 1,
  "partNo": "ZNO01"
}
```

写入成功返回结果:

```json
{"success": 1, "rtnmsg": "ok"}
```

写入失败返回结果示例:

```json
{"success": 0, "rtnmsg": "Invalid parameter:machineId"}
```

---

## 8、写入原料条码数据

### 8.1 新增原料条码

**写入参数说明:**

| 参数 | 数据类型 | 说明 |
|---|---|---|
| option | varchar(50) | API 标识, 此操作固定为 `addMaterialBarcode` |
| plant | varchar(50) | 工厂别，可为空 |
| machineId | int | 设备编号，如：1 或 2 等，不可为空 |
| barcode | varchar(30) | 原料条码，不可为空（只能包含字母、数字、中线和下划线且必须以字母或数字开头） |
| materialNo | varchar(50) | 原料编号，不可为空 |
| unitBarcodeWeight | decimal(10,3) | 单位条码重量(kg)，不可为空 |
| expirationDate | varchar(10) | 有效期，不可为空，格式: yyyy-MM-dd，如: 2019-09-20 |
| lotNo | varchar(50) | 生产批号，可为空 |

**写入方法:** HTTP POST
**数据格式:** JSON

写入数据示例如下:

```json
{
  "option": "addMaterialBarcode",
  "plant": "",
  "machineId": 1,
  "barcode": "ZNO190420000001",
  "materialNo": "ZNO01",
  "unitBarcodeWeight": 25.0,
  "expirationDate": "2019-09-20",
  "lotNo": ""
}
```

写入成功返回结果:

```json
{"success": 1, "rtnmsg": "ok"}
```

写入失败返回结果示例:

```json
{"success": 0, "rtnmsg": "Invalid parameter:machineId"}
```

### 8.2 修改原料条码

**写入参数说明:**

| 参数 | 数据类型 | 说明 |
|---|---|---|
| option | varchar(50) | API 标识, 此操作固定为 `updateMaterialBarcode` |
| plant | varchar(50) | 工厂别，可为空 |
| machineId | int | 设备编号，如：1 或 2 等，不可为空 |
| barcode | varchar(30) | 原料条码，不可为空 |
| materialNo | varchar(50) | 原料编号，不可为空 |
| unitBarcodeWeight | decimal(10,3) | 单位条码重量(kg)，不可为空 |
| expirationDate | varchar(10) | 有效期，不可为空，格式: yyyy-MM-dd，如: 2019-09-20 |
| lotNo | varchar(50) | 生产批号，可为空 |

**写入方法:** HTTP POST
**数据格式:** JSON

写入数据示例如下:

```json
{
  "option": "updateMaterialBarcode",
  "plant": "",
  "machineId": 1,
  "barcode": "ZNO190420000001",
  "materialNo": "ZNO01",
  "unitBarcodeWeight": 50.0,
  "expirationDate": "2019-09-25",
  "lotNo": ""
}
```

写入成功返回结果:

```json
{"success": 1, "rtnmsg": "ok"}
```

写入失败返回结果示例:

```json
{"success": 0, "rtnmsg": "Invalid parameter:machineId"}
```

### 8.3 删除原料条码

**写入参数说明:**

| 参数 | 数据类型 | 说明 |
|---|---|---|
| option | varchar(50) | API 标识, 此操作固定为 `deleteMaterialBarcode` |
| plant | varchar(50) | 工厂别，可为空 |
| machineId | int | 设备编号，如：1 或 2 等，不可为空 |
| barcode | varchar(30) | 原料条码，不可为空 |

**写入方法:** HTTP POST
**数据格式:** JSON

写入数据示例如下:

```json
{
  "option": "deleteMaterialBarcode",
  "plant": "",
  "machineId": 1,
  "barcode": "ZNO190420000001"
}
```

写入成功返回结果:

```json
{"success": 1, "rtnmsg": "ok"}
```

写入失败返回结果示例:

```json
{"success": 0, "rtnmsg": "Invalid parameter:machineId"}
```

---

## 9、写入配方数据

### 9.1 新增配方

**写入参数说明:**

| 参数 | 数据类型 | 说明 |
|---|---|---|
| option | varchar(50) | API 标识, 此操作固定为 `addFormula` |
| plant | varchar(50) | 工厂别，可为空 |
| machineId | int | 设备编号，如：1 或 2 等，不可为空 |
| formulaCode | varchar(50) | 配方编号，不可为空（只能包含字母、数字、中线和下划线且必须以字母或数字开头） |
| formulaName | varchar(50) | 配方名称，不可为空 |
| MaterialNo | varchar(50) | 原料编号，不可为空 |
| MaterialWeight | decimal(18,4) | 原料重量，单位: kg，不可为空 |

**写入方法:** HTTP POST
**数据格式:** JSON

写入数据示例如下:

```json
{
  "option": "addFormula",
  "plant": "",
  "machineId": 1,
  "formulaCode": "F001",
  "formulaName": "配方 01",
  "FormulaEntryList": [
    {"MaterialNo": "M01", "MaterialWeight": 0.25},
    {"MaterialNo": "M02", "MaterialWeight": 0.36}
  ]
}
```

写入成功返回结果:

```json
{"success": 1, "rtnmsg": "ok"}
```

写入失败返回结果示例:

```json
{"success": 0, "rtnmsg": "Invalid parameter:machineId"}
```

### 9.2 修改配方

**写入参数说明:**

| 参数 | 数据类型 | 说明 |
|---|---|---|
| option | varchar(50) | API 标识, 此操作固定为 `updateFormula` |
| plant | varchar(50) | 工厂别，可为空 |
| machineId | int | 设备编号，如：1 或 2 等，不可为空 |
| formulaCode | varchar(50) | 配方编号，不可为空 |
| formulaName | varchar(50) | 配方名称，不可为空 |
| MaterialNo | varchar(50) | 原料编号，不可为空 |
| MaterialWeight | decimal(18,4) | 原料重量，单位: kg，不可为空 |

**写入方法:** HTTP POST
**数据格式:** JSON

写入数据示例如下:

```json
{
  "option": "updateFormula",
  "plant": "",
  "machineId": 1,
  "formulaCode": "F001",
  "formulaName": "配方 01",
  "FormulaEntryList": [
    {"MaterialNo": "M01", "MaterialWeight": 0.55},
    {"MaterialNo": "M02", "MaterialWeight": 0.46}
  ]
}
```

写入成功返回结果:

```json
{"success": 1, "rtnmsg": "ok"}
```

写入失败返回结果示例:

```json
{"success": 0, "rtnmsg": "Invalid parameter:machineId"}
```

### 9.3 删除配方

**写入参数说明:**

| 参数 | 数据类型 | 说明 |
|---|---|---|
| option | varchar(50) | API 标识, 此操作固定为 `deleteFormula` |
| plant | varchar(50) | 工厂别，可为空 |
| machineId | int | 设备编号，如：1 或 2 等，不可为空 |
| formulaCode | varchar(50) | 配方编号，不可为空 |

**写入方法:** HTTP POST
**数据格式:** JSON

写入数据示例如下:

```json
{
  "option": "deleteFormula",
  "plant": "",
  "machineId": 1,
  "formulaCode": "F001"
}
```

写入成功返回结果:

```json
{"success": 1, "rtnmsg": "ok"}
```

写入失败返回结果示例:

```json
{"success": 0, "rtnmsg": "Invalid parameter:machineId"}
```

---

## 10、写入工单数据

### 10.1 新增工单

**写入参数说明:**

| 参数 | 数据类型 | 说明 |
|---|---|---|
| option | varchar(50) | API 标识, 此操作固定为 `addWorkOrder` |
| plant | varchar(50) | 工厂别，可为空 |
| machineId | int | 设备编号，如：1 或 2 等，不可为空 |
| workOrderNo | nvarchar(50) | 工单编号，不可为空（只能包含字母、数字、中线和下划线且必须以字母或数字开头） |
| workOrderDate | varchar(10) | 工单日期，不可为空，格式: yyyy-MM-dd，如: 2019-04-22 |
| formulaCode | varchar(50) | 配方编号，不可为空 |
| batch | int | 工单批次，不可为空 |

**写入方法:** HTTP POST
**数据格式:** JSON

写入数据示例如下:

```json
{
  "option": "addWorkOrder",
  "plant": "",
  "machineId": 1,
  "workOrderNo": "W0001",
  "workOrderDate": "2019-04-22",
  "formulaCode": "F001",
  "batch": 10
}
```

写入成功返回结果:

```json
{"success": 1, "rtnmsg": "ok"}
```

写入失败返回结果示例:

```json
{"success": 0, "rtnmsg": "Invalid parameter:machineId"}
```

### 10.2 修改工单

**写入参数说明:**

| 参数 | 数据类型 | 说明 |
|---|---|---|
| option | varchar(50) | API 标识, 此操作固定为 `updateWorkOrder` |
| plant | varchar(50) | 工厂别，可为空 |
| machineId | int | 设备编号，如：1 或 2 等，不可为空 |
| workOrderNo | nvarchar(50) | 工单编号，不可为空 |
| workOrderDate | varchar(10) | 工单日期，不可为空，格式: yyyy-MM-dd，如: 2019-04-23 |
| formulaCode | varchar(50) | 配方编号，不可为空 |
| batch | int | 工单批次，不可为空 |

**写入方法:** HTTP POST
**数据格式:** JSON

写入数据示例如下:

```json
{
  "option": "updateWorkOrder",
  "plant": "",
  "machineId": 1,
  "workOrderNo": "W0001",
  "workOrderDate": "2019-04-23",
  "formulaCode": "F001",
  "batch": 15
}
```

写入成功返回结果:

```json
{"success": 1, "rtnmsg": "ok"}
```

写入失败返回结果示例:

```json
{"success": 0, "rtnmsg": "Invalid parameter:machineId"}
```

### 10.3 删除工单

**写入参数说明:**

| 参数 | 数据类型 | 说明 |
|---|---|---|
| option | varchar(50) | API 标识, 此操作固定为 `deleteWorkOrder` |
| plant | varchar(50) | 工厂别，可为空 |
| machineId | int | 设备编号，如：1 或 2 等，不可为空 |
| workOrderNo | nvarchar(50) | 工单编号，不可为空 |

**写入方法:** HTTP POST
**数据格式:** JSON

写入数据示例如下:

```json
{
  "option": "deleteWorkOrder",
  "plant": "",
  "machineId": 1,
  "workOrderNo": "W0001"
}
```

写入成功返回结果:

```json
{"success": 1, "rtnmsg": "ok"}
```

写入失败返回结果示例:

```json
{"success": 0, "rtnmsg": "Invalid parameter:machineId"}
```

---

## 11、数据自动上传

客户提供接收数据 Http API 的 url 和写入成功返回结果(纯字符串或 json 格式)，机台自动向此 url 上传称量数据。

客户端接受上传数据若需要进行扣库存等业务操作，建议先把上传数据接收到本地并返回接收结果；然后针对本地接收的数据再进行相关业务操作，这样减少双方数据耦合性导致的数据上传失败重复上传数据问题以及业务数据紊乱问题。

**上传方法:** HTTP POST
**数据格式:** JSON

**上传数据字段说明:**

| 字段 | 数据类型 | 说明 |
|---|---|---|
| Plant | varchar(50) | 工厂别 |
| MachineID | int | 设备编号 |
| ShiftName | varchar(50) | 班别 |
| WorkOrderNo | nvarchar(50) | 工单编号 |
| OrderDate | varchar(10) | 工单日期 |
| FormulaCode | varchar(50) | 配方编号 |
| FormulaName | varchar(50) | 配方名称 |
| FormulaWeight | decimal(18,4) | 配方总重, 单位: kg |
| OrderState | int | 工单状态, 1:未生产 2:生产中 3:已完工 |
| OrderBatch | int | 工单计划批次 |
| Batch | int | 当前批次 |
| BatchWeight | decimal(18,4) | 当前批次重量, 单位: kg |
| Barcode | varchar(30) | 料号条码 |
| MfgTime | varchar(20) | 生产时间 |
| MaterialNo | varchar(50) | 原料编号 |
| MaterialName | varchar(100) | 原料名称 |
| MaterialWeight | decimal(18,4) | 配方原料重量, 单位: kg |
| WeighingValue | decimal(18,4) | 称量值, 单位: kg |
| MaterialBarcode | varchar(30) | 追溯的原料条码 |
| LotNo | varchar(50) | 追溯的原料批号 |
| Weight | decimal(18,4) | 追溯的原料重量, 单位: kg |
| AlarmCode | varchar(20) | 报警编号 |
| AlarmMsg | varchar(100) | 报警内容 |
| AlarmTime | varchar(20) | 报警时间 |

上传数据示例如下:

```json
{
  "Plant": "A1",
  "MachineID": 1,
  "ShiftName": "早",
  "WorkOrderNo": "A11190715001",
  "OrderDate": "2019-07-15",
  "FormulaCode": "F01",
  "FormulaName": "配方 01",
  "FormulaWeight": 0.64,
  "OrderState": 3,
  "OrderBatch": 10,
  "Batch": 4,
  "BatchWeight": 0.6405,
  "Barcode": "A1119071500110004",
  "MfgTime": "2019-07-15 11:40:59",
  "WorkOrderFormula": [
    {"MaterialNo": "M01", "MaterialName": "原料 01", "MaterialWeight": 0.4800},
    {"MaterialNo": "M02", "MaterialName": "原料 02", "MaterialWeight": 0.1600}
  ],
  "WeighingRecordList": [
    {
      "MaterialNo": "M01",
      "MaterialName": "原料 01",
      "WeighingValue": 0.4810,
      "MaterialTracedData": [
        {"MaterialBarcode": "190529003", "Weight": 0.379, "LotNo": "L1905037"},
        {"MaterialBarcode": "190528091", "Weight": 0.102, "LotNo": "L1905021"}
      ]
    },
    {
      "MaterialNo": "M02",
      "MaterialName": "原料 02",
      "WeighingValue": 0.1595,
      "MaterialTracedData": [
        {"MaterialBarcode": "190519023", "Weight": 0.1595, "LotNo": "L19050266"}
      ]
    }
  ],
  "AlarmList": [
    {
      "AlarmCode": "AD001",
      "AlarmMsg": "NO.07 Platform motor overload",
      "AlarmTime": "2019-07-15 11:34:21"
    }
  ]
}
```

---

## 12、API option 参数汇总

| API | Option |
|---|---|
| 查询工单数据 | `getWorkOrder` |
| 查询称量记录 | `getWeighingRecord` |
| 查询报警记录 | `getAlarmLog` |
| 依条码追溯生产记录 | `getTraceBackData` |
| 查询原料耗用 | `getMaterialConsumption` |
| 写入原料基本资料 - 新增 | `addParts` |
| 写入原料基本资料 - 修改 | `updateParts` |
| 写入原料基本资料 - 删除 | `deleteParts` |
| 写入原料条码数据 - 新增 | `addMaterialBarcode` |
| 写入原料条码数据 - 修改 | `updateMaterialBarcode` |
| 写入原料条码数据 - 删除 | `deleteMaterialBarcode` |
| 写入配方数据 - 新增 | `addFormula` |
| 写入配方数据 - 修改 | `updateFormula` |
| 写入配方数据 - 删除 | `deleteFormula` |
| 写入工单数据 - 新增 | `addWorkOrder` |
| 写入工单数据 - 修改 | `updateWorkOrder` |
| 写入工单数据 - 删除 | `deleteWorkOrder` |
