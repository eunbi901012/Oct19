package org.example.oct19.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.example.oct19.model.Resource;

/** Blocking MyBatis boundary over exactly the ten approved physical tables. */
@Mapper
public interface CatalogMapper {
    /** Reads physical rows using predicates added only for supplied non-null approved columns. */
    @SelectProvider(type = CatalogSql.class, method = "select")
    List<Map<String, Object>> select(@Param("resource") Resource resource,
            @Param("filters") Map<String, Object> filters);

    /** Saves only supplied physical columns; omitted optional properties are never reset on conflict. */
    @InsertProvider(type = CatalogSql.class, method = "save")
    int save(@Param("resource") Resource resource, @Param("row") Map<String, Object> row);

    /** Executes a real database roundtrip for public readiness. */
    @Select("""
            SELECT 1
            """)
    int ping();
}
