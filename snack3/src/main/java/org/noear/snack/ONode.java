package org.noear.snack;

import org.noear.snack.core.exts.Act1;
import org.noear.snack.core.exts.Act2;
import org.noear.snack.core.utils.NodeUtil;
import org.noear.snack.core.Constants;
import org.noear.snack.core.DEFAULTS;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 节点（One Node）
 * */
public class ONode {
    //内部配置
    protected Constants _c = Constants.def;
    //内部数据
    protected ONodeData _d = new ONodeData(this);
    //Null节点
    public static final ONode Null = new ONode();

    //版本信息
    public static String version(){return "3.0.12";}

    public ONode() {
    }

    public ONode(Constants cfg) {
        _c = cfg;
    }

    /** 初始化为 Object */
    public ONode asObject() {
        _d.tryInitObject(_c);
        return this;
    }

    /** 初始化为 Array */
    public ONode asArray() {
        _d.tryInitArray();
        return this;
    }

    /** 初始化为 Value */
    public ONode asValue() {
        _d.tryInitValue();
        return this;
    }

    /** 初始化为 Null */
    public ONode asNull() {
        _d.tryInitNull();
        return this;
    }

    /**
     * 节点数据
     */
    public ONodeData nodeData() {
        return _d;
    }
    /**
     * 节点类型
     * */
    public ONodeType nodeType() {
        return _d.nodeType;
    }

    /**
     * 切换配置
     * */
    public ONode cfg(Constants constants) {
        if (constants != null) {
            _c = constants;
        } else {
            _c = Constants.def;
        }
        return this;
    }


    /**
     * 返回自己，构建表达式
     */
    public ONode build(Act1<ONode> fun) {
        fun.run(this);
        return this;
    }
    public ONode exp(Act1<ONode> fun) {
        fun.run(this);
        return this;
    }


    ////////////////////
    //
    // 值处理
    //
    ////////////////////
    /**
     * 获取节点值
     * */
    public OValue val() {
        return asValue()._d.value;
    }

    /**
     * 节点赋值
     */
    public ONode val(Object val) {
        if (val == null) {
            _d.tryInitNull();
        } else if (val instanceof ONode) { //支持数据直接copy
            _d.tryInitNull();
            _d = ((ONode) val)._d;
        } else {
            _d.tryInitValue();
            _d.value.set(val);
        }

        return this;
    }


    /**
     * 获取 string 值
     */
    public String getString() {
        if (isValue()) {
            return _d.value.getString();
        } else {
            if(isArray()){
                return toJson();
            }

            if(isObject()){
                return toJson();
            }

            return _c.null_string;
        }
    }

    public short getShort() {
        if (isValue())
            return _d.value.getShort();
        else
            return 0;
    }

    /**
     * 获取 int 值
     */
    public int getInt() {
        if (isValue())
            return _d.value.getInt();
        else
            return 0;
    }

    /**
     * 获取 boolean 值
     */
    public boolean getBoolean() {
        if (isValue())
            return _d.value.getBoolean();
        else
            return false;
    }

    /**
     * 获取 long 值
     */
    public long getLong() {
        if (isValue())
            return _d.value.getLong();
        else
            return 0;
    }

    /**
     * 获取 date 值
     */
    public Date getDate() {
        if (isValue())
            return _d.value.getDate();
        else
            return null;
    }

    public float getFloat() {
        if (isValue())
            return _d.value.getFloat();
        else
            return 0;
    }

    /**
     * 获取 double 值
     */
    public double getDouble() {
        if (isValue())
            return _d.value.getDouble();
        else
            return 0;
    }

    /**
     * 获取 double 值
     */
    public double getDouble(int scale) {
        double temp = getDouble();

        if (temp == 0)
            return 0;
        else
            return new BigDecimal(temp)
                    .setScale(scale, BigDecimal.ROUND_HALF_UP)
                    .doubleValue();
    }

    public char getChar() {
        if (isValue())
            return _d.value.getChar();
        else
            return 0;
    }

    ////////////////////
    //
    // 对象与数组公共处理
    //
    ////////////////////

    /**
     * 清空子节点
     */
    public void clear() {
        if (isObject()) {
            _d.object.clear();
        } else if (isArray()) {
            _d.array.clear();
        }
    }

    public int count() {
        if (isObject()) {
            return _d.object.size();
        }

        if (isArray()) {
            return _d.array.size();
        }

        return 0;
    }

    ////////////////////
    //
    // 对象处理
    //
    ////////////////////
    /**
     * 获取节点对象
     * */
    public Map<String,ONode> obj() {
        return asObject()._d.object;
    }

    //是否存在节点
    public boolean contains(String key) {
        if (isObject()) {
            return _d.object.containsKey(key);
        } else {
            return false;
        }
    }

    /**
     * 返回对象子节点
     */

    public ONode get(String key) {
        _d.tryInitObject(_c);

        ONode tmp = _d.object.get(key);
        if (tmp == null && _c.null_node_new) {
            tmp = new ONode(_c);
            _d.object.put(key, tmp);
        }

        return tmp;
    }

    public ONode getNew(String key) {
        ONode tmp = new ONode(_c);
        _d.object.put(key, tmp);

        return tmp;
    }

    /**
     * 返回自己，设置对象子节点
     */
    public ONode set(String key, Object val) {
        _d.tryInitObject(_c);

        if (val instanceof ONode) {
            _d.object.put(key, ((ONode) val).cfg(_c));
        } else {
            _d.object.put(key, new ONode(_c).val(val));
        }

        return this;
    }

    /**
     * 设置对象子节点（需要手工提前初始化对象类型）
     */
    public void setNode(String key, ONode val) {
        _d.object.put(key, val);
    }

    /**
     * 返回自己，尝试添加对象型节点
     */
    public ONode setAll(ONode obj) {
        _d.tryInitObject(_c);

        if (obj != null && obj.isObject()) {
            _d.object.putAll(obj._d.object);
        }

        return this;
    }

    /**
     * 返回自己，尝试添加一个集合
     */
    public <T> ONode setAll(Map<String, T> map) {
        _d.tryInitObject(_c);

        if (map != null) {
            map.forEach((k, v) -> {
                set(k, v);
            });
        }
        return this;
    }

    /**
     * 返回自己，尝试添加一个集合，handler里获取的是自动产生的子节点
     */
    public <T> ONode setAll(Map<String, T> map, Act2<ONode, T> handler) {
        _d.tryInitObject(_c);

        if (map != null) {
            map.forEach((k, v) -> {
                handler.run(this.get(k), v);
            });
        }
        return this;
    }

    /**
     * 移除对象子节点(搞不清楚是自身还是被移除的，所以不返回)
     */
    public void remove(String key) {
        _d.tryInitObject(_c);
        _d.object.remove(key);
    }



    ////////////////////
    //
    // 数组处理
    //
    ////////////////////
    /**
     * 获取节点对象
     * */
    public List<ONode> ary() {
        return asArray()._d.array;
    }

    /**
     * 获取数组项
     */
    public ONode get(int index) {
        _d.tryInitArray();

        if (_d.array.size() > index) {
            return _d.array.get(index);
        } else {
            if(_c.null_node_new){
                return new ONode();
            }else {
                return null;
            }
        }
    }

    /**
     * 移除数组子节点(搞不清楚是自身还是被移除的，所以不返回)
     */
    public void removeAt(int index) {
        _d.tryInitArray();
        _d.array.remove(index);
    }

    /**
     * 返回数组子节点，创建数据新的子节点
     */
    public ONode addNew() {
        _d.tryInitArray();
        ONode n = new ONode().cfg(_c);
        _d.array.add(n);
        return n;
    }

    /**
     * 返回自己，为数组添加子节点
     */
    public ONode add(Object val) {
        _d.tryInitArray();

        if (val instanceof ONode) {
            _d.array.add((ONode) val);
        } else {
            _d.array.add(new ONode(_c).val(val));
        }

        return this;
    }

    /**
     * 添加节点（需要手工提前初始化数组类型）
     */
    public void addNode(ONode val) {
        _d.array.add(val);
    }

    /**
     * 返回自己，尝试添加数组型节点
     */
    public ONode addAll(ONode ary) {
        _d.tryInitArray();

        if (ary != null && ary.isArray()) {
            _d.array.addAll(ary._d.array);
        }

        return this;
    }

    /**
     * 返回自己，尝试添加一个集合
     */
    public <T> ONode addAll(Collection<T> ary) {
        _d.tryInitArray();

        if (ary != null) {
            ary.forEach(m -> add(m));
        }
        return this;
    }

    /**
     * 返回自己，尝试添加一个集合，handler里获取的是自动产生的子节点
     */
    public <T> ONode addAll(Collection<T> ary, Act2<ONode, T> handler) {
        _d.tryInitArray();

        if (ary != null) {
            ary.forEach(m -> handler.run(addNew(), m));
        }
        return this;
    }

    //////////////////////

    public boolean isNull() {
        return (_d.nodeType == ONodeType.Null) || (isValue() && _d.value.isNull());
    }

    public boolean isValue() {
        return _d.nodeType == ONodeType.Value;
    }

    public boolean isObject() {
        return _d.nodeType == ONodeType.Object;
    }

    public boolean isArray() {
        return _d.nodeType == ONodeType.Array;
    }

    //////////////////////


    /**
     * 遍历对象
     */
    public void forEach(BiConsumer<String, ONode> consumer) {
        if (isObject()) {
            _d.object.forEach(consumer);
        }
    }

    /**
     * 遍历数组
     */
    public void forEach(Consumer<ONode> consumer) {
        if (isArray()) {
            _d.array.forEach(consumer);
        }
    }

    ////////////////////
    //
    // 特性处理
    //
    ////////////////////

    /**
     * 获取特性
     */
    public String attrGet(String key) {
        return _d.attrGet(key);
    }

    /**
     * 设置特性
     */
    public void attrSet(String key, String val) {
        _d.attrSet(key, val);
    }

    public void attrForeach(BiConsumer<String, String> consumer) {
        if (_d.attrs != null) {
            _d.attrs.forEach(consumer);
        }
    }

    ////////////////////
    //
    // 数据转换
    //
    ////////////////////

    @Override
    public String toString() {
        return NodeUtil.toStr(_c, this, _c.stringToer);
    }

    /**
     * 将当前ONode 转为 json string
     */
    public String toJson() {
        return NodeUtil.toStr(_c, this, DEFAULTS.DEF_JSON_TOER);
    }

    /**
     * 将当前ONode 转为 Map or List or val
     */
    public Object toData() {
        return NodeUtil.toObj(_c, this, ONode.class, DEFAULTS.DEF_DATA_TOER);
    }

    /**
     * 将当前ONode 转为 Object
     */
    public <T> T toBean(Class<T> clz) {
        return (T) NodeUtil.toObj(_c, this, clz, _c.objectToer);
    }


    /**
     * 填充数据，并返回自己
     * */
    public ONode fill(Object source) {
        ONode tmp = load(source);
        val(tmp);
        return this;
    }

    /**
     * 填充数据，并返回自己（从来源处加载数据，并做为自己的值），来源：bean object
     */
    public ONode fillObj(Object source) throws Exception{
        ONode tmp = loadObj(source);
        val(tmp);
        return this;
    }

    /**
     * 填充数据，并返回自己（从来源处加载数据，并做为自己的值），来源：string
     */
    public ONode fillStr(String source) throws Exception{
        ONode tmp = loadStr(source);
        val(tmp);
        return this;
    }

    ////////////////////
    //
    // 来源加载
    //
    ////////////////////

    public static ONode load(Object source) {
        try {
            if (source instanceof String) {
                return NodeUtil.fromStr((String) source);
            }
            return NodeUtil.fromObj(source);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ONode();
        }
    }

    /**
     * 加载来源：string （返回可能为null 或有异常）
     */
    public static ONode loadStr(String source) throws Exception{
        return NodeUtil.fromStr(source);
    }
    /**
     * 加载来源：bean object （返回可能为null 或有异常）
     */
    public static ONode loadObj(Object source) throws Exception{
        return NodeUtil.fromObj(source);
    }

    /**
     * 序列化为 json string
     */
    public static String serialize(Object source)  throws Exception {
        return NodeUtil.fromObj(Constants.serialize, source).toJson();
    }

    public static String serialize(Object source, Constants constants)  throws Exception {
        return NodeUtil.fromObj(constants, source).toJson();
    }

    /**
     * 反序列化为 Object
     */
    public static <T> T deserialize(String source, Class<?> clz) throws Exception{
        return (T)NodeUtil.fromStr(Constants.serialize, source).toBean(clz);
    }

    public static <T> T deserialize(String source, Class<?> clz, Constants constants)  throws Exception{
        return (T)NodeUtil.fromStr(constants, source).toBean(clz);
    }
}