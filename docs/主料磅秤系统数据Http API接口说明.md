# 主料磅秤系统数据 Http API 接口说明

## 1、接口 URL 定义

- 查询数据默认接口 URL 为 `http://192.168.99.202:8000/mws/api`
- 写入数据默认接口 URL 为 `http://192.168.99.202:8000/mws/api/wr`

> IP 地址最终以实际地址为准，以下内容均以此 url 举例

---

## 2、查询工单数据

### 2.1 请求说明

**请求地址:**

```
http://192.168.99.202:8000/mws/api?option=getWorkOrder&plant=PLANT&machineId=MACHINEID&workOrderNo=WORKORDERNO&formulaCode=FORMULACODE&orderDateFrom=ORDERDATEFROM&orderDateTo=ORDERDATETO&orderState=ORDERSTATE
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
      "WorkOrderNo": "A1301810110001",
      "OrderDate": "2018-10-11",
      "LineNo": "A",
      "FormulaCode": "TXG2-1",
      "OrderBatchNum": 27,
      "OrderWeight": 4037.5,
      "FinishBatchNum": 25,
      "FinishWeight": 4060.56,
      "StartTime": "2018-10-11 13:40:51",
      "FinishTime": "2018-10-11 18:03:39",
      "OrderState": 3
    },
    {
      "WorkOrderNo": "A1301810120001",
      "OrderDate": "2018-10-12",
      "LineNo": "A",
      "FormulaCode": "TXG2-1",
      "OrderBatchNum": 25,
      "OrderWeight": 161.5,
      "FinishBatchNum": 1,
      "FinishWeight": 161.18,
      "StartTime": "2018-10-12 13:11:50",
      "FinishTime": "2018-10-12 13:26:37",
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
| LineNo | 产线编号，主要用于一供二机台，有效值:A/B |
| FormulaCode | 配方编号 |
| OrderBatchNum | 批次数 |
| OrderWeight | 工单总重(kg) |
| FinishBatchNum | 完工批次数 |
| FinishWeight | 完工重量(kg) |
| StartTime | 生产开始时间 |
| FinishTime | 生产结束时间 |
| OrderState | 工单状态，1:未生产 2:生产中 3:已完工 |

---

## 3、查询称量记录

### 3.1 请求说明

**请求地址:**

```
http://192.168.99.202:8000/mws/api?option=getWeighingRecord&plant=PLANT&machineId=MACHINEID&workOrderNo=WORKORDERNO&formulaCode=FORMULACODE&weighTimeFrom=WEIGHTIMEFROM&weighTimeTo=WEIGHTIMETO
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
      "WorkOrderNo": "A1301810120001",
      "FormulaCode": "TXG2-1",
      "Batch": 1,
      "MaterialNo": "C2",
      "WeighingValue": 9.4,
      "WeighTime": "2018-10-12 13:26:37",
      "IsError": 0
    },
    {
      "WorkOrderNo": "A1301810120001",
      "FormulaCode": "TXG2-1",
      "Batch": 1,
      "MaterialNo": "C5",
      "WeighingValue": 39.9,
      "WeighTime": "2018-10-12 13:26:37",
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
http://192.168.99.202:8000/mws/api?option=getAlarmLog&plant=PLANT&machineId=MACHINEID&alarmGroup=ALARMGROUP&alarmTimeFrom=ALARMTIMEFROM&alarmTimeTo=ALARMTIMETO
```

**请求方法:** HTTP GET

**请求参数说明:**

| 参数 | 说明 |
|---|---|
| option | API 标识, 此请求固定为 `getAlarmLog` |
| plant | 工厂别，可为空 |
| machineId | 设备编号，如：1 或 2 等，不可为空 |
| alarmGroup | 报警类别，可为空。1-1号秤异常;2-2号秤异常;3-3号秤异常;0-胶料秤异常;10-密炼机异常;100-其他异常 |
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
      "AlarmGroup": "0",
      "AlarmGroupText": "胶料秤异常",
      "AlarmCode": "A113",
      "AlarmMsg": "1 区 7 号原料桶低料位",
      "AlarmTime": "2018-10-10 18:01:58",
      "WorkOrderNo": "A1301810100001",
      "Batch": 25
    },
    {
      "AlarmGroup": "0",
      "AlarmGroupText": "胶料秤异常",
      "AlarmCode": "A108",
      "AlarmMsg": "1 区 2 号原料桶低料位",
      "AlarmTime": "2018-10-10 17:08:18",
      "WorkOrderNo": "A1301810100001",
      "Batch": 21
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

## 5、查询原料耗用

### 5.1 请求说明

**请求地址:**

```
http://192.168.99.202:8000/mws/api?option=getMaterialConsumption&plant=PLANT&machineId=MACHINEID&workOrderNo=WORKORDERNO&materialNo=MATERIALNO&consTimeFrom=CONSTIMEFROM&consTimeTo=CONSTIMETO
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

### 5.2 返回结果

**结果数据格式:** JSON

查询正确时返回结果示例如下：

```json
{
  "success": 1,
  "rtnmsg": "ok",
  "rtndata": [
    {
      "ConsumptionTime": "2018-10-10",
      "MaterialNo": "C1-PVC 树脂",
      "WeighingValue": 2497.8
    },
    {
      "ConsumptionTime": "2018-10-10",
      "MaterialNo": "C2-CPE",
      "WeighingValue": 254.54
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

## 6、查询密炼机工艺数据

### 6.1 请求说明

**请求地址:**

```
http://192.168.99.202:8000/mws/api?option=getProcessData&plant=PLANT&machineId=MACHINEID&workOrderNo=WORKORDERNO&batch=BATCH
```

**请求方法:** HTTP GET

**请求参数说明:**

| 参数 | 说明 |
|---|---|
| option | API 标识, 此请求固定为 `getProcessData` |
| plant | 工厂别，可为空 |
| machineId | 设备编号，如：1 或 2 等，不可为空 |
| workOrderNo | 工单编号，不可为空 |
| batch | 工单批次，不可为空 |

### 6.2 返回结果

**结果数据格式:** JSON

查询正确时返回结果示例如下：

```json
{
  "success": 1,
  "rtnmsg": "ok",
  "rtndata": [
    {
      "Time": "2018-10-10 15:06:09",
      "Pressure": 55.2,
      "Speed": 25.0,
      "Temperature": 71.6,
      "Power": 16.7,
      "Current": 117.8
    },
    {
      "Time": "2018-10-10 15:06:19",
      "Pressure": 55.3,
      "Speed": 25.0,
      "Temperature": 72.7,
      "Power": 16.7,
      "Current": 118.1
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
| Time | 生产时间 |
| Pressure | 压力(bar) |
| Speed | 转速(rpm) |
| Temperature | 温度(℃) |
| Power | 功率(kw) |
| Current | 电流(A) |

---

## 7、写入原料基本资料

### 7.1 新增原料基本资料

**写入参数说明:**

| 参数 | 数据类型 | 说明 |
|---|---|---|
| option | varchar(50) | API 标识, 此操作固定为 `addParts` |
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
{"success": 0, "rtnmsg": "Invalid parameter:option"}
```

### 7.2 修改原料基本资料

**写入参数说明:**

| 参数 | 数据类型 | 说明 |
|---|---|---|
| option | varchar(50) | API 标识, 此操作固定为 `updateParts` |
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
{"success": 0, "rtnmsg": "Invalid parameter:option"}
```

### 7.3 删除原料基本资料

**写入参数说明:**

| 参数 | 数据类型 | 说明 |
|---|---|---|
| option | varchar(50) | API 标识, 此操作固定为 `deleteParts` |
| partNo | varchar(50) | 原料编号，不可为空 |

**写入方法:** HTTP POST
**数据格式:** JSON

写入数据示例如下:

```json
{
  "option": "deleteParts",
  "partNo": "ZNO01"
}
```

写入成功返回结果:

```json
{"success": 1, "rtnmsg": "ok"}
```

写入失败返回结果示例:

```json
{"success": 0, "rtnmsg": "Invalid parameter:option"}
```

---

## 8、写入原料条码

### 8.1 新增原料条码

**写入参数说明:**

| 参数 | 数据类型 | 说明 |
|---|---|---|
| option | varchar(50) | API 标识, 此操作固定为 `addMaterialBarcode` |
| barcode | varchar(30) | 原料条码，不可为空（只能包含字母、数字、中线和下划线且必须以字母或数字开头） |
| materialNo | varchar(50) | 原料编号，不可为空 |
| unitBarcodeWeight | decimal(10,3) | 条码重量(kg)，不可为空 |
| expirationDate | varchar(10) | 有效期，不可为空，格式:yyyy-MM-dd |
| lotNo | varchar(50) | 原料批号，可为空 |

**写入方法:** HTTP POST
**数据格式:** JSON

写入数据示例如下:

```json
{
  "option": "addMaterialBarcode",
  "barcode": "B000000001",
  "materialNo": "ZNO01",
  "unitBarcodeWeight": 50.0,
  "expirationDate": "2019-09-30",
  "lotNo": "L19060000001"
}
```

写入成功返回结果:

```json
{"success": 1, "rtnmsg": "ok"}
```

写入失败返回结果示例:

```json
{"success": 0, "rtnmsg": "Invalid parameter:option"}
```

### 8.2 修改原料条码

**写入参数说明:**

| 参数 | 数据类型 | 说明 |
|---|---|---|
| option | varchar(50) | API 标识, 此操作固定为 `updateMaterialBarcode` |
| barcode | varchar(30) | 原料条码，不可为空 |
| materialNo | varchar(50) | 原料编号，不可为空 |
| unitBarcodeWeight | decimal(10,3) | 条码重量(kg)，不可为空 |
| expirationDate | varchar(10) | 有效期，不可为空，格式:yyyy-MM-dd |
| lotNo | varchar(50) | 原料批号，可为空 |

**写入方法:** HTTP POST
**数据格式:** JSON

写入数据示例如下:

```json
{
  "option": "updateMaterialBarcode",
  "barcode": "B000000002",
  "materialNo": "ZNO01",
  "unitBarcodeWeight": 50.0,
  "expirationDate": "2019-10-27",
  "lotNo": "L19050008"
}
```

写入成功返回结果:

```json
{"success": 1, "rtnmsg": "ok"}
```

写入失败返回结果示例:

```json
{"success": 0, "rtnmsg": "Invalid parameter:option"}
```

### 8.3 删除原料条码

**写入参数说明:**

| 参数 | 数据类型 | 说明 |
|---|---|---|
| option | varchar(50) | API 标识, 此操作固定为 `deleteMaterialBarcode` |
| barcode | varchar(30) | 原料条码，不可为空 |

**写入方法:** HTTP POST
**数据格式:** JSON

写入数据示例如下:

```json
{
  "option": "deleteMaterialBarcode",
  "barcode": "B000000002"
}
```

写入成功返回结果:

```json
{"success": 1, "rtnmsg": "ok"}
```

写入失败返回结果示例:

```json
{"success": 0, "rtnmsg": "Invalid parameter:option"}
```

---

## 9、写入配方

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
| StepNo | int | 下料段序，不可为空；同一段不可有多个相同原料；段序从 1 顺序开始，中间不可有间隔 |

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
    {"MaterialNo": "ZNO01", "MaterialWeight": 28.0, "StepNo": 1},
    {"MaterialNo": "ZNO02", "MaterialWeight": 61.0, "StepNo": 1}
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
| StepNo | int | 下料段序，不可为空；同一段不可有多个相同原料；段序从 1 顺序开始，中间不可有间隔 |

**写入方法:** HTTP POST
**数据格式:** JSON

写入数据示例如下:

```json
{
  "option": "updateFormula",
  "plant": "",
  "machineId": 1,
  "formulaCode": "F001",
  "formulaName": "配方 001",
  "FormulaEntryList": [
    {"MaterialNo": "ZNO01", "MaterialWeight": 20.0, "StepNo": 1},
    {"MaterialNo": "ZNO02", "MaterialWeight": 61.5, "StepNo": 1}
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

## 10、写入工单

### 10.1 新增工单

**写入参数说明:**

| 参数 | 数据类型 | 说明 |
|---|---|---|
| option | varchar(50) | API 标识, 此操作固定为 `addWorkOrder` |
| plant | varchar(50) | 工厂别，可为空 |
| machineId | int | 设备编号，如：1 或 2 等，不可为空 |
| workOrderNo | nvarchar(50) | 工单编号，不可为空（只能包含字母、数字、中线和下划线且必须以字母或数字开头） |
| workOrderDate | varchar(10) | 工单日期，不可为空，格式: yyyy-MM-dd，如: 2019-06-26 |
| formulaCode | varchar(50) | 配方编号，不可为空 |
| batch | int | 工单批次，不可为空 |
| lineNo | varchar(10) | 产线编号，主要用于一供二机台，有效值:A/B。一供一机台可为空，一供二机台不可为空 |

**写入方法:** HTTP POST
**数据格式:** JSON

写入数据示例如下:

```json
{
  "option": "addWorkOrder",
  "plant": "",
  "machineId": 1,
  "workOrderNo": "W0001",
  "workOrderDate": "2019-06-26",
  "formulaCode": "F001",
  "batch": 10,
  "lineNo": ""
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
| workOrderDate | varchar(10) | 工单日期，不可为空，格式: yyyy-MM-dd，如: 2019-06-26 |
| formulaCode | varchar(50) | 配方编号，不可为空 |
| batch | int | 工单批次，不可为空 |
| lineNo | varchar(10) | 产线编号，主要用于一供二机台，有效值:A/B。一供一机台可为空，一供二机台不可为空 |

**写入方法:** HTTP POST
**数据格式:** JSON

写入数据示例如下:

```json
{
  "option": "updateWorkOrder",
  "plant": "",
  "machineId": 1,
  "workOrderNo": "W0001",
  "workOrderDate": "2019-06-26",
  "formulaCode": "F001",
  "batch": 18,
  "lineNo": ""
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
| LineNo | varchar(10) | 产线编号，主要用于一供二机台，有效值:A/B |
| FormulaCode | varchar(50) | 配方编号 |
| FormulaName | varchar(50) | 配方名称 |
| FormulaWeight | decimal(18,4) | 配方总重, 单位: kg |
| OrderState | int | 工单状态, 1:未生产 2:生产中 3:已完工 |
| OrderBatch | int | 工单计划批次 |
| Batch | int | 当前批次 |
| BatchWeight | decimal(18,4) | 当前批次重量, 单位: kg |
| MfgTime | varchar(20) | 生产时间 |
| MaterialNo | varchar(50) | 原料编号 |
| MaterialName | varchar(100) | 原料名称 |
| MaterialWeight | decimal(18,4) | 配方原料重量, 单位: kg |
| StepNo | int | 排料段序 |
| WeighingValue | decimal(18,4) | 称量值, 单位: kg |
| WeighingTime | varchar(20) | 开始称量时间 |
| MaterialBarcode | varchar(30) | 耗用的原料条码 |
| Weight | decimal(18,4) | 耗用条码的原料重量, 单位: kg |
| AlarmCode | varchar(20) | 报警编号 |
| AlarmMsg | varchar(100) | 报警内容 |
| AlarmTime | varchar(20) | 报警时间 |

上传数据示例如下:

```json
{
  "Plant": "A1",
  "MachineID": 1,
  "ShiftName": "早",
  "WorkOrderNo": "A11906030001",
  "OrderDate": "2019-06-03",
  "LineNo": "A",
  "FormulaCode": "FC-1A",
  "FormulaName": "TM7001",
  "FormulaWeight": 61.3,
  "OrderState": 2,
  "OrderBatch": 10,
  "Batch": 3,
  "BatchWeight": 38.9500,
  "MfgTime": "2019-06-03 15:25:25",
  "WorkOrderRecipe": [
    {"MaterialNo": "M001", "MaterialName": "N550", "MaterialWeight": 12.5000, "StepNo": 1},
    {"MaterialNo": "M002", "MaterialName": "N770", "MaterialWeight": 26.6000, "StepNo": 1}
  ],
  "WeighingRecordList": [
    {
      "MaterialNo": "M001",
      "MaterialName": "N550",
      "WeighingValue": 12.3900,
      "StepNo": 1,
      "WeighingTime": "2019-06-03 15:12:14",
      "MaterialBarcodeList": [
        {"MaterialBarcode": "A2019060300099", "Weight": 12.3900}
      ]
    },
    {
      "MaterialNo": "M002",
      "MaterialName": "N770",
      "WeighingValue": 26.5600,
      "StepNo": 1,
      "WeighingTime": "2019-06-03 15:14:04",
      "MaterialBarcodeList": []
    }
  ],
  "AlarmList": [
    {
      "AlarmCode": "A120",
      "AlarmMsg": "Root's blower overload",
      "AlarmTime": "2019-06-03 15:13:36"
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
| 查询密炼机工艺数据 | `getProcessData` |
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
