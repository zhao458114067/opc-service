# OPC-DEMO

### java测试opc连接

读值接口get请求：
* localhost:8880/opc/readTag/tag1,tag2

写值接口put请求
* localhost:8880/opc/write

请求体

{
"tag1": 301,
"tag2": 301
}