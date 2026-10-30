package com.buschmais.jqassistant.commandline.task.mcp.tool.getschema.model;

import java.util.Optional;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class Property {

    private String name;

    private Type type;

    public enum Type {
        STRING,
        INTEGER,
        FLOAT,
        DATETIME,
        BOOLEAN;

        public static Optional<Type> of(Class<?> type) {
            if (String.class.equals(type) || Enum.class.isAssignableFrom(type)) {
                return Optional.of(Type.STRING);
            } else if (Integer.class.equals(type) || int.class.equals(type) || Long.class.equals(type) || long.class.equals(type)) {
                return Optional.of(Type.INTEGER);
            } else if (Number.class.equals(type) || Float.class.equals(type) || float.class.equals(type) || Double.class.equals(type) || double.class.equals(
                    type)) {
                return Optional.of(Type.FLOAT);
                //            } else if (DateTime.class.equals(type)) {
                //                return Optional.of(Type.DATETIME);
            } else if (Boolean.class.equals(type) || boolean.class.equals(type)) {
                return Optional.of(Type.BOOLEAN);
            } else {
                System.out.println(type);
                return Optional.empty();
            }
        }
    }

}
