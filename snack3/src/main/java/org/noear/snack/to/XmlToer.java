package org.noear.snack.to;

import org.noear.snack.ONode;
import org.noear.snack.core.Context;
import org.noear.snack.core.exts.ThData;

public class XmlToer implements Toer {
    private static final String attr_name = "@name";
    private static final ThData<StringBuilder> tlBuilder = new ThData(() -> new StringBuilder(1024 * 5));


    @Override
    public void handle(Context ctx) throws Exception {
        ONode o = (ONode) ctx.source;

        if (null != o) {
            StringBuilder sb = tlBuilder.get(); // new StringBuilder(1024*5); //
            sb.setLength(0);

            handle_do(o, sb, "xml");

            ctx.target = sb.toString();
        }
    }

    private void handle_do(ONode o, StringBuilder sb, String p_name) {

        if (o.isArray()) {
            String name = o.attrGet(attr_name);
            if(name == null){
                name = p_name;
            }

            sb.append("<").append(name);
            o.attrMap().forEach((k, v) -> {
                if (attr_name.equals(k) == false) {
                    sb.append(" ").append(k).append("=").append("\"").append(v).append("\"");
                }
            });
            sb.append(">");

            for(ONode n : o.ary()){
                handle_do(n, sb, name);
            }

            sb.append("</").append(name).append(">");

            return;
        }

        if (o.isObject()) {
            o.forEach((k, v) -> {
                //如果没有节点名，给它加一个
                if (v.attrMap().containsKey(attr_name) == false) {
                    v.attrSet(attr_name, k);
                }

                handle_do(v, sb, p_name);
            });
            return;
        }

        sb.append(o.getString());

    }
}