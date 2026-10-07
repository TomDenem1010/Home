package trd.home;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import trd.home.auth.dao.User;
import trd.home.common.dao.ApplicationEvent;
import trd.home.common.dao.ApplicationLog;
import trd.home.media.dao.Actor;
import trd.home.media.dao.Folder;
import trd.home.media.dao.Video;
import trd.home.tcg.dao.CardmarketCard;
import trd.home.tcg.dao.CardmarketCardPrice;
import trd.home.tcg.dao.CardmarketDeck;
import trd.home.tcg.dao.CardmarketDeckCard;
import trd.home.tcg.dao.CardmarketDeckVersion;

class SchemaMappingTest {

    @Test
    void explicitIndexesDoNotDuplicateIndexesCreatedByPrimaryOrUniqueConstraints() throws IOException {
        String schema = schema();
        var tables = Pattern.compile("CREATE TABLE (\\w+) \\((.*?)\\n\\);", Pattern.DOTALL)
                .matcher(schema);
        while (tables.find()) {
            String tableName = tables.group(1);
            var constraints =
                    Pattern.compile("(?:PRIMARY KEY|UNIQUE) \\(([^)]+)\\)").matcher(tables.group(2));
            while (constraints.find()) {
                String columns = constraints.group(1);
                var index = Pattern.compile("CREATE (?:UNIQUE )?INDEX \\w+\\s+ON " + Pattern.quote(tableName)
                                + "\\s*\\(" + Pattern.quote(columns) + "\\)")
                        .matcher(schema);
                assertTrue(!index.find(), tableName + " has a redundant index on " + columns);
            }
        }
    }

    @ParameterizedTest
    @ValueSource(
            classes = {
                CardmarketCard.class,
                CardmarketCardPrice.class,
                CardmarketDeck.class,
                CardmarketDeckVersion.class,
                CardmarketDeckCard.class,
                User.class,
                ApplicationEvent.class,
                ApplicationLog.class,
                Actor.class,
                Folder.class,
                Video.class
            })
    void explicitColumnConstraintsMatchFlywaySchema(Class<?> entity) throws IOException {
        String schema = schema();
        String table = tableDefinition(schema, entity);
        for (Class<?> type = entity; type != Object.class; type = type.getSuperclass()) {
            for (var field : type.getDeclaredFields()) {
                if (field.isAnnotationPresent(CollectionTable.class)) {
                    continue;
                }
                Column column = field.getAnnotation(Column.class);
                if (column != null) {
                    String name = column.name().isEmpty()
                            ? field.getName().replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                            : column.name();
                    var definition = Pattern.compile("(?m)^\\s*" + name + "\\s+([^\\n]+)", Pattern.CASE_INSENSITIVE)
                            .matcher(table);
                    assertTrue(definition.find(), entity.getSimpleName() + "." + field.getName());
                    String sql = definition.group(1).toUpperCase(Locale.ROOT);
                    assertEquals(!sql.contains("NOT NULL"), column.nullable(), name);
                    if (field.isAnnotationPresent(Lob.class)) {
                        assertTrue(sql.startsWith("CLOB"), name);
                    } else if (field.getType() == String.class
                            || field.getType().isEnum()) {
                        assertTrue(
                                sql.startsWith("VARCHAR2(" + column.length() + ")")
                                        || sql.startsWith("VARCHAR2(" + column.length() + " CHAR)"),
                                name);
                    } else if (field.getType() == Instant.class) {
                        assertTrue(sql.startsWith("TIMESTAMP(6)"), name);
                    }
                    if (column.precision() > 0) {
                        assertTrue(sql.startsWith("NUMBER(" + column.precision() + ", " + column.scale() + ")"), name);
                    }
                    if (column.unique()) {
                        assertUnique(schema, entity, name);
                    }
                }
                JoinColumn join = field.getAnnotation(JoinColumn.class);
                if (join != null) {
                    var definition = Pattern.compile(
                                    "(?m)^\\s*" + join.name() + "\\s+([^\\n]+)", Pattern.CASE_INSENSITIVE)
                            .matcher(table);
                    assertTrue(definition.find(), join.name());
                    assertEquals(!definition.group(1).contains("NOT NULL"), join.nullable(), join.name());
                    if (join.unique() || field.isAnnotationPresent(OneToOne.class)) {
                        assertUnique(schema, entity, join.name());
                    }
                }
            }
        }
    }

    @ParameterizedTest
    @ValueSource(
            classes = {
                CardmarketCard.class,
                CardmarketDeck.class,
                CardmarketDeckVersion.class,
                CardmarketDeckCard.class,
                Actor.class,
                Folder.class,
                Video.class
            })
    void tableConstraintsMatchFlywaySchema(Class<?> entity) throws IOException {
        String schema = schema();
        Table table = entity.getAnnotation(Table.class);
        for (var unique : table.uniqueConstraints()) {
            assertUnique(schema, entity, unique.columnNames());
        }
        for (var check : table.check()) {
            assertTrue(
                    schema.contains(("CONSTRAINT " + check.name() + " CHECK (" + check.constraint() + ")")
                            .toUpperCase(Locale.ROOT)),
                    check.name());
        }
    }

    @ParameterizedTest
    @ValueSource(classes = {User.class, Video.class})
    void collectionConstraintsMatchFlywaySchema(Class<?> entity) throws IOException {
        String schema = schema();
        for (var field : entity.getDeclaredFields()) {
            CollectionTable collection = field.getAnnotation(CollectionTable.class);
            JoinTable join = field.getAnnotation(JoinTable.class);
            String name;
            JoinColumn[] columns;
            JoinColumn[] inverseColumns;
            UniqueConstraint[] constraints;
            if (collection != null) {
                name = collection.name();
                columns = collection.joinColumns();
                inverseColumns = new JoinColumn[0];
                constraints = collection.uniqueConstraints();
            } else if (join != null) {
                name = join.name();
                columns = join.joinColumns();
                inverseColumns = join.inverseJoinColumns();
                constraints = join.uniqueConstraints();
            } else {
                continue;
            }
            String table = tableDefinition(schema, name);
            for (var column : columns) {
                assertRequiredJoin(table, column);
            }
            for (var column : inverseColumns) {
                assertRequiredJoin(table, column);
            }
            if (collection != null) {
                Column column = java.util.Objects.requireNonNull(field.getAnnotation(Column.class));
                assertTrue(table.contains(column.name() + " VARCHAR2(" + column.length() + ") NOT NULL"));
                assertEquals(false, column.nullable());
            }
            assertTrue(constraints.length > 0, name);
            for (var constraint : constraints) {
                String names = String.join(", ", constraint.columnNames()).toUpperCase(Locale.ROOT);
                assertTrue(
                        table.contains("PRIMARY KEY (" + names + ")") || table.contains("UNIQUE (" + names + ")"),
                        name);
            }
        }
    }

    private static void assertRequiredJoin(String table, JoinColumn column) {
        assertEquals(false, column.nullable(), column.name());
        assertTrue(table.contains(column.name().toUpperCase(Locale.ROOT) + " VARCHAR2(255) NOT NULL"), column.name());
    }

    private static void assertUnique(String schema, Class<?> entity, String... columns) {
        String names = String.join(
                ", ",
                Arrays.stream(columns)
                        .map(name -> name.toUpperCase(Locale.ROOT))
                        .toList());
        String table = tableDefinition(schema, entity);
        String indexTarget =
                "ON " + entity.getAnnotation(Table.class).name().toUpperCase(Locale.ROOT) + " (" + names + ")";
        assertTrue(
                table.contains("UNIQUE (" + names + ")")
                        || Pattern.compile("CREATE UNIQUE INDEX \\w+\\s+" + Pattern.quote(indexTarget))
                                .matcher(schema)
                                .find(),
                entity.getSimpleName() + " unique " + names);
    }

    private static String tableDefinition(String schema, Class<?> entity) {
        return tableDefinition(schema, entity.getAnnotation(Table.class).name());
    }

    private static String tableDefinition(String schema, String tableName) {
        String name = tableName.toUpperCase(Locale.ROOT);
        int start = schema.indexOf("CREATE TABLE " + name + " (");
        assertTrue(start >= 0, name);
        return schema.substring(start, schema.indexOf("\n);", start));
    }

    private static String schema() throws IOException {
        StringBuilder schema = new StringBuilder();
        for (String migration : new String[] {
            "V1__TCG_CARDMARKET.sql",
            "V2__AUTH.sql",
            "V3__APPLICATION_EVENT.sql",
            "V4__APPLICATION_LOG.sql",
            "V5__MEDIA.sql"
        }) {
            try (var resource = SchemaMappingTest.class.getResourceAsStream("/db/migration/" + migration)) {
                schema.append(
                        new String(java.util.Objects.requireNonNull(resource).readAllBytes(), StandardCharsets.UTF_8));
            }
        }
        return schema.toString().replace("\r\n", "\n").toUpperCase(Locale.ROOT);
    }
}
