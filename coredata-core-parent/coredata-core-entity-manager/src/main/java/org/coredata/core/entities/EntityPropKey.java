package org.coredata.core.entities;

import org.neo4j.ogm.annotation.Index;

public class EntityPropKey {

    /**
     * 是否启用该资产
     */
    public static final String enable = "enable";


    /**
     * 管理IP
     */
    public static final String mainIp="mainIp";


    /**
     * 资源节点显示名称
     */
    public static final String displayName="displayName";

    /**
     * 资源节点索引
     */
    public static final String index="index";

    /**
     * 对应采集模型id，例如：mysql，mysql_database
     */
    public static final String modelId="modelId";

    /**
     * 采集清洗id
     */
    public static final String transformId="transformId";

    /**
     * 挖掘清洗id
     */
    public static final String dataminingId="dataminingId";

    /**
     * 告警模型id
     */
    public static final String decisionId="decisionId";


    /**
     * 用于保存根资源节点的唯一标识
     */
    public static final String rootInstId="rootInstId";

    /**
     * 表明资源版本信息
     */
    public static final String version="version";

    /**
     * 表明资源所属厂商
     */
    public static final String vendor="vendor";


    /**
     * 是否加入监控，默认不加入
     */
    public static final String isMonitor = "isMonitor";

    /**
     * 是否可以被监控，默认为是
     */
    public static final String canMonitor = "canMonitor";

    /**
     * 是否为资产（可以被显示在前台列表）
     */
    public static final String isAsset = "isAsset";

    /**
     * 是否已经加入监控，默认否
     */
    public static final String addMonitor = "addMonitor";

    /**
     * 由拓扑发现传来的节点标识，唯一
     */
    public static final String ntmId="ntmId";

    /**
     * 源设备ID
     */
    public static final String link_srcId="link_srcId";

    /**
     * 源接口索引
     */
    public static final String link_srcifindex="link_srcifindex";

    /**
     * 目标设备实例ID
     */
    public static final String link_destId="link_destId";

    /**
     * 目标接口索引
     */
    public static final String link_destifindex="link_destifindex";

    /**
     * 是否主干链路
     */
    public static final String isBackBone="isBackBone";

    /**
     * 链路类型
     */
    public static final String link_type="link_type";

    /**
     * 带宽
     */
    public static final String bandwidth="bandwidth";

    /**
     * 采集索引
     */
    public static final String collectIndex="collectIndex";

    /**
     * 资产类型
     */
    @Index
    public static final String resType="resType";

    /**
     * 资产类型全路径
     */
    @Index
    public static final String resfullType="resfullType";

    /**
     * 资产型号
     */
    public static final String resModel="resModel";

    /**
     * 重要度
     */
    public static final String importance="importance";

    /**
     * 场域位置
     */
    public static final String location="location";

    /**
     * 管理位置
     */
    public static final String adminLocation="adminLocation";

    /**
     * 拥有类型
     */
    public static final String ownType="ownType";

    /**
     * 所属部门
     */
    public static final String department="department";

    /**
     * 管理员
     */
    public static final String admin="admin";

    /**
     * 使用者
     */
    public static final String user="user";

    /**
     * 供应商
     */
    public static final String supplier="supplier";

    /**
     * 采购日期
     */
    public static final String procurementTime="procurementTime";

    /**
     * 维保日期
     */
    public static final String maintenanceTime="maintenanceTime";

    /**
     * 作废日期
     */
    public static final String voidTime="voidTime";

    /**
     * 用途
     */
    public static final String useFor="useFor";

    /**
     * 资产编号
     */
    public static final String resNo="resNo";

    /**
     * 资产状态对应，维修报废等等
     */
    public static final String resStatus="resStatus";

    /**
     * 资产的类型树
     */
    public static final String resModelPath="resModelPath";

    /**
     * 规格
     */
    public static final String resModelSpec="resModelSpec";

    /**
     * 型号
     */
    public static final String modelNumber="modelNumber";


    /**
     * 告警策略ID
     */
    public static final String decisionPolicy="decisionPolicy";

    /**
     * 监控策略ID
     */
    public static final String collectPolicy="collectPolicy";

    /**
     * 资产类型的扩展属性
     */
    public static final String extendProperties="extendProperties";

    /**
     * 实体类型，软件，硬件等
     */
    public static final String entityType="entityType";

    /**
     * 对应restype的中文名称
     */
    public static final String resTypeName="resTypeName";

    /**
     * 对应resfulltype的中文路径
     */
    public static final String resfullTypeName="resfullTypeName";

    /**
     * 用于保存拓扑节点发现时的相关信息
     */
    public static final String node="node";
}
