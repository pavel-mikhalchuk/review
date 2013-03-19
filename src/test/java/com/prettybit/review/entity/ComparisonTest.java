package com.prettybit.review.entity;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

/**
 * @author Pavel Mikhalchuk
 */
public class ComparisonTest {

    @Test
    public void testRealComparison() throws IOException {
        List<Line> base = toLines("package com.shc.obu.app.framework.jdbc.shard.search;\n" +
                "\n" +
                "import java.util.Collection;\n" +
                "import java.util.HashMap;\n" +
                "import java.util.Map;\n" +
                "\n" +
                "import org.apache.commons.dbcp.BasicDataSource;\n" +
                "\n" +
                "import com.google.common.base.Preconditions;\n" +
                "import com.google.common.base.Predicate;\n" +
                "import com.google.common.collect.Collections2;\n" +
                "import com.shc.obu.app.framework.enums.Applications;\n" +
                "import com.shc.obu.app.framework.jdbc.shard.Shard;\n" +
                "import com.shc.obu.app.framework.jdbc.shard.ShardLocator;\n" +
                "import com.shc.obu.app.framework.jdbc.shard.ShardType;\n" +
                "\n" +
                "public class SearchShardLocator {\n" +
                "\n" +
                "    private static SearchShardSelector<String>                    typeShardSelector;\n" +
                "\n" +
                "    private static Map<SolrCoreType, Collection<? extends Shard>> sshardsMap = new HashMap<SolrCoreType, Collection<? extends Shard>>();\n" +
                "\n" +
                "    public static synchronized void init(BasicDataSource shardDs) {\n" +
                "        if (sshardsMap == null || sshardsMap.isEmpty()) {\n" +
                "            Preconditions.checkNotNull(shardDs, \"Shard information datasource cannot be null !\");\n" +
                "            typeShardSelector = new SearchShardSelector<String>(new TypeShardMapper(shardDs), new SearchShardReader(shardDs));\n" +
                "            sshardsMap = typeShardSelector.getSearchShardsByShardType();\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    public static Map<SolrCoreType, Collection<? extends Shard>> getSearchShardsMap() {\n" +
                "        return sshardsMap;\n" +
                "    }\n" +
                "\n" +
                "    public static SearchShard searchCatalogShard(long accountId) {\n" +
                "        final int dbShardIndex = ShardLocator.catalogShardIndex(accountId);\n" +
                "        ShardLocator.getShardsOf(ShardType.CATALOG);\n" +
                "\n" +
                "        for (SearchShard searchCatalogShard : bothCatalogSearchShards()) {\n" +
                "            final int shardIndex = searchCatalogShard.getDbShardIdx();\n" +
                "            if (shardIndex == dbShardIndex)\n" +
                "                return searchCatalogShard;\n" +
                "        }\n" +
                "\n" +
                "        return null;\n" +
                "    }\n" +
                "\n" +
                "    public static Collection<SearchShard> sellpoCatalogSearchShards() {\n" +
                "        return filterByTypeApplication(Applications.SELLPO, SolrCoreType.CATALOG, false);\n" +
                "    }\n" +
                "\n" +
                "    public static Collection<SearchShard> spinCatalogSearchShards() {\n" +
                "        return filterByTypeApplication(Applications.SPIN, SolrCoreType.CATALOG, false);\n" +
                "    }\n" +
                "\n" +
                "    public static Collection<SearchShard> bothCatalogSearchShards() {\n" +
                "        return filterByTypeApplication(Applications.BOTH, SolrCoreType.CATALOG, false);\n" +
                "    }\n" +
                "\n" +
                "    private static Collection<SearchShard> filterByTypeApplication(final Applications mask, final SolrCoreType type, final Boolean isMaster) {\n" +
                "        @SuppressWarnings(\"unchecked\")\n" +
                "        final Collection<SearchShard> shardsByType = (Collection<SearchShard>) getSearchShardsByType(type);\n" +
                "\n" +
                "        Collection<SearchShard> appSS = Collections2.filter(shardsByType, new Predicate<SearchShard>() {\n" +
                "\n" +
                "            @Override\n" +
                "            public boolean apply(SearchShard input) {\n" +
                "\n" +
                "                if (((input.getAppMask() & mask.getId()) != 0) && (isMaster == null || input.isMaster() == isMaster))\n" +
                "                    return true;\n" +
                "                return false;\n" +
                "            }\n" +
                "\n" +
                "        });\n" +
                "        return appSS;\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "    private static Collection<? extends Shard> getSearchShardsByType(SolrCoreType type) {\n" +
                "        final Collection<? extends Shard> shardsByType = sshardsMap.get(type);\n" +
                "        return shardsByType;\n" +
                "    }\n" +
                "\n" +
                "    public static Collection<SearchShard> getSearchShardsByApplicationType(Applications mask, SolrCoreType type) {\n" +
                "        return filterByTypeApplication(mask, type, false);\n" +
                "    }\n" +
                "\n" +
                "    public static Collection<SearchShard> getSlaveShardsByApplicationType(Applications mask, SolrCoreType type) {\n" +
                "        return filterByTypeApplication(mask, type, false);\n" +
                "    }\n" +
                "\n" +
                "    public static Collection<SearchShard> getMasterShardsByApplicationType(Applications mask, SolrCoreType type) {\n" +
                "        return filterByTypeApplication(mask, type, true);\n" +
                "    }\n" +
                "\n" +
                "}\n");

        Diff diff = Diff.parse("===================================================================\n" +
                "--- jdbc/src/main/java/com/shc/obu/app/framework/jdbc/shard/search/SearchShardLocator.java\t(revision 124713)\n" +
                "+++ jdbc/src/main/java/com/shc/obu/app/framework/jdbc/shard/search/SearchShardLocator.java\t(revision 124714)\n" +
                "@@ -46,18 +46,18 @@\n" +
                "     }\n" +
                " \n" +
                "     public static Collection<SearchShard> sellpoCatalogSearchShards() {\n" +
                "-        return filterByTypeApplication(Applications.SELLPO, SolrCoreType.CATALOG, false);\n" +
                "+        return filterByTypeApplication(Applications.SELLPO, SolrCoreType.CATALOG, false,null);\n" +
                "     }\n" +
                " \n" +
                "     public static Collection<SearchShard> spinCatalogSearchShards() {\n" +
                "-        return filterByTypeApplication(Applications.SPIN, SolrCoreType.CATALOG, false);\n" +
                "+        return filterByTypeApplication(Applications.SPIN, SolrCoreType.CATALOG, false,null);\n" +
                "     }\n" +
                " \n" +
                "     public static Collection<SearchShard> bothCatalogSearchShards() {\n" +
                "-        return filterByTypeApplication(Applications.BOTH, SolrCoreType.CATALOG, false);\n" +
                "+        return filterByTypeApplication(Applications.BOTH, SolrCoreType.CATALOG, false,null);\n" +
                "     }\n" +
                " \n" +
                "-    private static Collection<SearchShard> filterByTypeApplication(final Applications mask, final SolrCoreType type, final Boolean isMaster) {\n" +
                "+    private static Collection<SearchShard> filterByTypeApplication(final Applications mask, final SolrCoreType type, final Boolean isMaster, final Integer dbShardIndex) {\n" +
                "         @SuppressWarnings(\"unchecked\")\n" +
                "         final Collection<SearchShard> shardsByType = (Collection<SearchShard>) getSearchShardsByType(type);\n" +
                " \n" +
                "@@ -66,7 +66,11 @@\n" +
                "             @Override\n" +
                "             public boolean apply(SearchShard input) {\n" +
                " \n" +
                "-                if (((input.getAppMask() & mask.getId()) != 0) && (isMaster == null || input.isMaster() == isMaster))\n" +
                "+                if (\n" +
                "+                \t\t((input.getAppMask() & mask.getId()) != 0) && \n" +
                "+                \t\t(isMaster == null || input.isMaster() == isMaster) && \n" +
                "+                \t\t(dbShardIndex == null || input.getDbShardIdx() == dbShardIndex)\n" +
                "+                   )\n" +
                "                     return true;\n" +
                "                 return false;\n" +
                "             }\n" +
                "@@ -82,15 +86,19 @@\n" +
                "     }\n" +
                " \n" +
                "     public static Collection<SearchShard> getSearchShardsByApplicationType(Applications mask, SolrCoreType type) {\n" +
                "-        return filterByTypeApplication(mask, type, false);\n" +
                "+        return filterByTypeApplication(mask, type, false,null);\n" +
                "     }\n" +
                " \n" +
                "     public static Collection<SearchShard> getSlaveShardsByApplicationType(Applications mask, SolrCoreType type) {\n" +
                "-        return filterByTypeApplication(mask, type, false);\n" +
                "+        return filterByTypeApplication(mask, type, false,null);\n" +
                "     }\n" +
                " \n" +
                "     public static Collection<SearchShard> getMasterShardsByApplicationType(Applications mask, SolrCoreType type) {\n" +
                "-        return filterByTypeApplication(mask, type, true);\n" +
                "+        return filterByTypeApplication(mask, type, true,null);\n" +
                "     }\n" +
                "+    \n" +
                "+    public static Collection<SearchShard> getSlaveShards(Applications mask, SolrCoreType type, Integer dbShardIndex) {\n" +
                "+        return filterByTypeApplication(mask, type, false,dbShardIndex);\n" +
                "+    }\n" +
                " \n" +
                " }\n");

        Comparison c = new Comparison(base, diff);

        assertEquals(104, c.left().size());
        assertEquals(new Line(1, "package com.shc.obu.app.framework.jdbc.shard.search;"), c.left().get(0));
        assertEquals(new Line(2, ""), c.left().get(1));
        assertEquals(new Line(3, "import java.util.Collection;"), c.left().get(2));
        assertEquals(new Line(4, "import java.util.HashMap;"), c.left().get(3));
        assertEquals(new Line(5, "import java.util.Map;"), c.left().get(4));
        assertEquals(new Line(6, ""), c.left().get(5));
        assertEquals(new Line(7, "import org.apache.commons.dbcp.BasicDataSource;"), c.left().get(6));
        assertEquals(new Line(8, ""), c.left().get(7));
        assertEquals(new Line(9, "import com.google.common.base.Preconditions;"), c.left().get(8));
        assertEquals(new Line(10, "import com.google.common.base.Predicate;"), c.left().get(9));
        assertEquals(new Line(11, "import com.google.common.collect.Collections2;"), c.left().get(10));
        assertEquals(new Line(12, "import com.shc.obu.app.framework.enums.Applications;"), c.left().get(11));
        assertEquals(new Line(13, "import com.shc.obu.app.framework.jdbc.shard.Shard;"), c.left().get(12));
        assertEquals(new Line(14, "import com.shc.obu.app.framework.jdbc.shard.ShardLocator;"), c.left().get(13));
        assertEquals(new Line(15, "import com.shc.obu.app.framework.jdbc.shard.ShardType;"), c.left().get(14));
        assertEquals(new Line(16, ""), c.left().get(15));
        assertEquals(new Line(17, "public class SearchShardLocator {"), c.left().get(16));
        assertEquals(new Line(18, ""), c.left().get(17));
        assertEquals(new Line(19, "    private static SearchShardSelector<String>                    typeShardSelector;"), c.left().get(18));
        assertEquals(new Line(20, ""), c.left().get(19));
        assertEquals(new Line(21, "    private static Map<SolrCoreType, Collection<? extends Shard>> sshardsMap = new HashMap<SolrCoreType, Collection<? extends Shard>>();"), c.left().get(20));
        assertEquals(new Line(22, ""), c.left().get(21));
        assertEquals(new Line(23, "    public static synchronized void init(BasicDataSource shardDs) {"), c.left().get(22));
        assertEquals(new Line(24, "        if (sshardsMap == null || sshardsMap.isEmpty()) {"), c.left().get(23));
        assertEquals(new Line(25, "            Preconditions.checkNotNull(shardDs, \"Shard information datasource cannot be null !\");"), c.left().get(24));
        assertEquals(new Line(26, "            typeShardSelector = new SearchShardSelector<String>(new TypeShardMapper(shardDs), new SearchShardReader(shardDs));"), c.left().get(25));
        assertEquals(new Line(27, "            sshardsMap = typeShardSelector.getSearchShardsByShardType();"), c.left().get(26));
        assertEquals(new Line(28, "        }"), c.left().get(27));
        assertEquals(new Line(29, "    }"), c.left().get(28));
        assertEquals(new Line(30, ""), c.left().get(29));
        assertEquals(new Line(31, "    public static Map<SolrCoreType, Collection<? extends Shard>> getSearchShardsMap() {"), c.left().get(30));
        assertEquals(new Line(32, "        return sshardsMap;"), c.left().get(31));
        assertEquals(new Line(33, "    }"), c.left().get(32));
        assertEquals(new Line(34, ""), c.left().get(33));
        assertEquals(new Line(35, "    public static SearchShard searchCatalogShard(long accountId) {"), c.left().get(34));
        assertEquals(new Line(36, "        final int dbShardIndex = ShardLocator.catalogShardIndex(accountId);"), c.left().get(35));
        assertEquals(new Line(37, "        ShardLocator.getShardsOf(ShardType.CATALOG);"), c.left().get(36));
        assertEquals(new Line(38, ""), c.left().get(37));
        assertEquals(new Line(39, "        for (SearchShard searchCatalogShard : bothCatalogSearchShards()) {"), c.left().get(38));
        assertEquals(new Line(40, "            final int shardIndex = searchCatalogShard.getDbShardIdx();"), c.left().get(39));
        assertEquals(new Line(41, "            if (shardIndex == dbShardIndex)"), c.left().get(40));
        assertEquals(new Line(42, "                return searchCatalogShard;"), c.left().get(41));
        assertEquals(new Line(43, "        }"), c.left().get(42));
        assertEquals(new Line(44, ""), c.left().get(43));
        assertEquals(new Line(45, "        return null;"), c.left().get(44));
        assertEquals(new Line(46, "    }"), c.left().get(45));
        assertEquals(new Line(47, ""), c.left().get(46));
        assertEquals(new Line(48, "    public static Collection<SearchShard> sellpoCatalogSearchShards() {"), c.left().get(47));
        assertEquals(new Line(49, "        return filterByTypeApplication(Applications.SELLPO, SolrCoreType.CATALOG, false|$+||+$|);", "!"), c.left().get(48));
        assertEquals(new Line(50, "    }"), c.left().get(49));
        assertEquals(new Line(51, ""), c.left().get(50));
        assertEquals(new Line(52, "    public static Collection<SearchShard> spinCatalogSearchShards() {"), c.left().get(51));
        assertEquals(new Line(53, "        return filterByTypeApplication(Applications.SPIN, SolrCoreType.CATALOG, false|$+||+$|);", "!"), c.left().get(52));
        assertEquals(new Line(54, "    }"), c.left().get(53));
        assertEquals(new Line(55, ""), c.left().get(54));
        assertEquals(new Line(56, "    public static Collection<SearchShard> bothCatalogSearchShards() {"), c.left().get(55));
        assertEquals(new Line(57, "        return filterByTypeApplication(Applications.BOTH, SolrCoreType.CATALOG, false|$+||+$|);", "!"), c.left().get(56));
        assertEquals(new Line(58, "    }"), c.left().get(57));
        assertEquals(new Line(59, ""), c.left().get(58));
        assertEquals(new Line(60, "    private static Collection<SearchShard> filterByTypeApplication(final Applications mask, final SolrCoreType type, final Boolean isMaster|$+||+$|) {", "!"), c.left().get(59));
        assertEquals(new Line(61, "        @SuppressWarnings(\"unchecked\")"), c.left().get(60));
        assertEquals(new Line(62, "        final Collection<SearchShard> shardsByType = (Collection<SearchShard>) getSearchShardsByType(type);"), c.left().get(61));
        assertEquals(new Line(63, ""), c.left().get(62));
        assertEquals(new Line(64, "        Collection<SearchShard> appSS = Collections2.filter(shardsByType, new Predicate<SearchShard>() {"), c.left().get(63));
        assertEquals(new Line(65, ""), c.left().get(64));
        assertEquals(new Line(66, "            @Override"), c.left().get(65));
        assertEquals(new Line(67, "            public boolean apply(SearchShard input) {"), c.left().get(66));
        assertEquals(new Line(68, ""), c.left().get(67));
        assertEquals(new Line(69, "                if (|$-|((input.getAppMask() & mask.getId()) != 0) && (isMaster == null || input.isMaster() == isMaster))|-$|", "!"), c.left().get(68));
        assertEquals(new Line(70, "", "+"), c.left().get(69));
        assertEquals(new Line(71, "", "+"), c.left().get(70));
        assertEquals(new Line(72, "", "+"), c.left().get(71));
        assertEquals(new Line(73, "", "+"), c.left().get(72));
        assertEquals(new Line(74, "                    return true;"), c.left().get(73));
        assertEquals(new Line(75, "                return false;"), c.left().get(74));
        assertEquals(new Line(76, "            }"), c.left().get(75));
        assertEquals(new Line(77, ""), c.left().get(76));
        assertEquals(new Line(78, "        });"), c.left().get(77));
        assertEquals(new Line(79, "        return appSS;"), c.left().get(78));
        assertEquals(new Line(80, ""), c.left().get(79));
        assertEquals(new Line(81, "    }"), c.left().get(80));
        assertEquals(new Line(82, ""), c.left().get(81));
        assertEquals(new Line(83, "    private static Collection<? extends Shard> getSearchShardsByType(SolrCoreType type) {"), c.left().get(82));
        assertEquals(new Line(84, "        final Collection<? extends Shard> shardsByType = sshardsMap.get(type);"), c.left().get(83));
        assertEquals(new Line(85, "        return shardsByType;"), c.left().get(84));
        assertEquals(new Line(86, "    }"), c.left().get(85));
        assertEquals(new Line(87, ""), c.left().get(86));
        assertEquals(new Line(88, "    public static Collection<SearchShard> getSearchShardsByApplicationType(Applications mask, SolrCoreType type) {"), c.left().get(87));
        assertEquals(new Line(89, "        return filterByTypeApplication(mask, type, false);", "!"), c.left().get(88));
        assertEquals(new Line(90, "    }"), c.left().get(89));
        assertEquals(new Line(91, ""), c.left().get(90));
        assertEquals(new Line(92, "    public static Collection<SearchShard> getSlaveShardsByApplicationType(Applications mask, SolrCoreType type) {"), c.left().get(91));
        assertEquals(new Line(93, "        return filterByTypeApplication(mask, type, false|$+||+$|);", "!"), c.left().get(92));
        assertEquals(new Line(94, "    }"), c.left().get(93));
        assertEquals(new Line(95, ""), c.left().get(94));
        assertEquals(new Line(96, "    public static Collection<SearchShard> getMasterShardsByApplicationType(Applications mask, SolrCoreType type) {"), c.left().get(95));
        assertEquals(new Line(97, "        return filterByTypeApplication(mask, type, true|$+||+$|);", "!"), c.left().get(96));
        assertEquals(new Line(98, "    }"), c.left().get(97));
        assertEquals(new Line(99, "", "+"), c.left().get(98));
        assertEquals(new Line(100, "", "+"), c.left().get(99));
        assertEquals(new Line(101, "", "+"), c.left().get(100));
        assertEquals(new Line(102, "", "+"), c.left().get(101));
        assertEquals(new Line(103, ""), c.left().get(102));
        assertEquals(new Line(104, "}"), c.left().get(103));

        assertEquals(104, c.right().size());
        assertEquals(new Line(1, "package com.shc.obu.app.framework.jdbc.shard.search;"), c.right().get(0));
        assertEquals(new Line(2, ""), c.right().get(1));
        assertEquals(new Line(3, "import java.util.Collection;"), c.right().get(2));
        assertEquals(new Line(4, "import java.util.HashMap;"), c.right().get(3));
        assertEquals(new Line(5, "import java.util.Map;"), c.right().get(4));
        assertEquals(new Line(6, ""), c.right().get(5));
        assertEquals(new Line(7, "import org.apache.commons.dbcp.BasicDataSource;"), c.right().get(6));
        assertEquals(new Line(8, ""), c.right().get(7));
        assertEquals(new Line(9, "import com.google.common.base.Preconditions;"), c.right().get(8));
        assertEquals(new Line(10, "import com.google.common.base.Predicate;"), c.right().get(9));
        assertEquals(new Line(11, "import com.google.common.collect.Collections2;"), c.right().get(10));
        assertEquals(new Line(12, "import com.shc.obu.app.framework.enums.Applications;"), c.right().get(11));
        assertEquals(new Line(13, "import com.shc.obu.app.framework.jdbc.shard.Shard;"), c.right().get(12));
        assertEquals(new Line(14, "import com.shc.obu.app.framework.jdbc.shard.ShardLocator;"), c.right().get(13));
        assertEquals(new Line(15, "import com.shc.obu.app.framework.jdbc.shard.ShardType;"), c.right().get(14));
        assertEquals(new Line(16, ""), c.right().get(15));
        assertEquals(new Line(17, "public class SearchShardLocator {"), c.right().get(16));
        assertEquals(new Line(18, ""), c.right().get(17));
        assertEquals(new Line(19, "    private static SearchShardSelector<String>                    typeShardSelector;"), c.right().get(18));
        assertEquals(new Line(20, ""), c.right().get(19));
        assertEquals(new Line(21, "    private static Map<SolrCoreType, Collection<? extends Shard>> sshardsMap = new HashMap<SolrCoreType, Collection<? extends Shard>>();"), c.right().get(20));
        assertEquals(new Line(22, ""), c.right().get(21));
        assertEquals(new Line(23, "    public static synchronized void init(BasicDataSource shardDs) {"), c.right().get(22));
        assertEquals(new Line(24, "        if (sshardsMap == null || sshardsMap.isEmpty()) {"), c.right().get(23));
        assertEquals(new Line(25, "            Preconditions.checkNotNull(shardDs, \"Shard information datasource cannot be null !\");"), c.right().get(24));
        assertEquals(new Line(26, "            typeShardSelector = new SearchShardSelector<String>(new TypeShardMapper(shardDs), new SearchShardReader(shardDs));"), c.right().get(25));
        assertEquals(new Line(27, "            sshardsMap = typeShardSelector.getSearchShardsByShardType();"), c.right().get(26));
        assertEquals(new Line(28, "        }"), c.right().get(27));
        assertEquals(new Line(29, "    }"), c.right().get(28));
        assertEquals(new Line(30, ""), c.right().get(29));
        assertEquals(new Line(31, "    public static Map<SolrCoreType, Collection<? extends Shard>> getSearchShardsMap() {"), c.right().get(30));
        assertEquals(new Line(32, "        return sshardsMap;"), c.right().get(31));
        assertEquals(new Line(33, "    }"), c.right().get(32));
        assertEquals(new Line(34, ""), c.right().get(33));
        assertEquals(new Line(35, "    public static SearchShard searchCatalogShard(long accountId) {"), c.right().get(34));
        assertEquals(new Line(36, "        final int dbShardIndex = ShardLocator.catalogShardIndex(accountId);"), c.right().get(35));
        assertEquals(new Line(37, "        ShardLocator.getShardsOf(ShardType.CATALOG);"), c.right().get(36));
        assertEquals(new Line(38, ""), c.right().get(37));
        assertEquals(new Line(39, "        for (SearchShard searchCatalogShard : bothCatalogSearchShards()) {"), c.right().get(38));
        assertEquals(new Line(40, "            final int shardIndex = searchCatalogShard.getDbShardIdx();"), c.right().get(39));
        assertEquals(new Line(41, "            if (shardIndex == dbShardIndex)"), c.right().get(40));
        assertEquals(new Line(42, "                return searchCatalogShard;"), c.right().get(41));
        assertEquals(new Line(43, "        }"), c.right().get(42));
        assertEquals(new Line(44, ""), c.right().get(43));
        assertEquals(new Line(45, "        return null;"), c.right().get(44));
        assertEquals(new Line(46, "    }"), c.right().get(45));
        assertEquals(new Line(47, ""), c.right().get(46));
        assertEquals(new Line(48, "    public static Collection<SearchShard> sellpoCatalogSearchShards() {"), c.right().get(47));
        assertEquals(new Line(49, "        return filterByTypeApplication(Applications.SELLPO, SolrCoreType.CATALOG, false|$+|,null|+$|);", "!"), c.right().get(48));
        assertEquals(new Line(50, "    }"), c.right().get(49));
        assertEquals(new Line(51, ""), c.right().get(50));
        assertEquals(new Line(52, "    public static Collection<SearchShard> spinCatalogSearchShards() {"), c.right().get(51));
        assertEquals(new Line(53, "        return filterByTypeApplication(Applications.SPIN, SolrCoreType.CATALOG, false|$+|,null|+$|);", "!"), c.right().get(52));
        assertEquals(new Line(54, "    }"), c.right().get(53));
        assertEquals(new Line(55, ""), c.right().get(54));
        assertEquals(new Line(56, "    public static Collection<SearchShard> bothCatalogSearchShards() {"), c.right().get(55));
        assertEquals(new Line(57, "        return filterByTypeApplication(Applications.BOTH, SolrCoreType.CATALOG, false|$+|,null|+$|);", "!"), c.right().get(56));
        assertEquals(new Line(58, "    }"), c.right().get(57));
        assertEquals(new Line(59, ""), c.right().get(58));
        assertEquals(new Line(60, "    private static Collection<SearchShard> filterByTypeApplication(final Applications mask, final SolrCoreType type, final Boolean isMaster|$+|, final Integer dbShardIndex|+$|) {", "!"), c.right().get(59));
        assertEquals(new Line(61, "        @SuppressWarnings(\"unchecked\")"), c.right().get(60));
        assertEquals(new Line(62, "        final Collection<SearchShard> shardsByType = (Collection<SearchShard>) getSearchShardsByType(type);"), c.right().get(61));
        assertEquals(new Line(63, ""), c.right().get(62));
        assertEquals(new Line(64, "        Collection<SearchShard> appSS = Collections2.filter(shardsByType, new Predicate<SearchShard>() {"), c.right().get(63));
        assertEquals(new Line(65, ""), c.right().get(64));
        assertEquals(new Line(66, "            @Override"), c.right().get(65));
        assertEquals(new Line(67, "            public boolean apply(SearchShard input) {"), c.right().get(66));
        assertEquals(new Line(68, ""), c.right().get(67));
        assertEquals(new Line(69, "                if (", "!"), c.right().get(68));
        assertEquals(new Line(70, "                \t\t((input.getAppMask() & mask.getId()) != 0) && ", "+"), c.right().get(69));
        assertEquals(new Line(71, "                \t\t(isMaster == null || input.isMaster() == isMaster) && ", "+"), c.right().get(70));
        assertEquals(new Line(72, "                \t\t(dbShardIndex == null || input.getDbShardIdx() == dbShardIndex)", "+"), c.right().get(71));
        assertEquals(new Line(73, "                   )", "+"), c.right().get(72));
        assertEquals(new Line(74, "                    return true;"), c.left().get(73));
        assertEquals(new Line(75, "                return false;"), c.right().get(74));
        assertEquals(new Line(76, "            }"), c.right().get(75));
        assertEquals(new Line(77, ""), c.right().get(76));
        assertEquals(new Line(78, "        });"), c.right().get(77));
        assertEquals(new Line(79, "        return appSS;"), c.right().get(78));
        assertEquals(new Line(80, ""), c.right().get(79));
        assertEquals(new Line(81, "    }"), c.right().get(80));
        assertEquals(new Line(82, ""), c.right().get(81));
        assertEquals(new Line(83, "    private static Collection<? extends Shard> getSearchShardsByType(SolrCoreType type) {"), c.right().get(82));
        assertEquals(new Line(84, "        final Collection<? extends Shard> shardsByType = sshardsMap.get(type);"), c.right().get(83));
        assertEquals(new Line(85, "        return shardsByType;"), c.right().get(84));
        assertEquals(new Line(86, "    }"), c.right().get(85));
        assertEquals(new Line(87, ""), c.right().get(86));
        assertEquals(new Line(88, "    public static Collection<SearchShard> getSearchShardsByApplicationType(Applications mask, SolrCoreType type) {"), c.right().get(87));
        assertEquals(new Line(89, "        return filterByTypeApplication(mask, type, false|$+|,null|+$|);", "!"), c.right().get(88));
        assertEquals(new Line(90, "    }"), c.right().get(89));
        assertEquals(new Line(91, ""), c.right().get(90));
        assertEquals(new Line(92, "    public static Collection<SearchShard> getSlaveShardsByApplicationType(Applications mask, SolrCoreType type) {"), c.right().get(91));
        assertEquals(new Line(93, "        return filterByTypeApplication(mask, type, false|$+|,null|+$|);", "!"), c.right().get(92));
        assertEquals(new Line(94, "    }"), c.right().get(93));
        assertEquals(new Line(95, ""), c.right().get(94));
        assertEquals(new Line(96, "    public static Collection<SearchShard> getMasterShardsByApplicationType(Applications mask, SolrCoreType type) {"), c.right().get(95));
        assertEquals(new Line(97, "        return filterByTypeApplication(mask, type, true|$+|,null|+$|);", "!"), c.right().get(96));
        assertEquals(new Line(98, "    }"), c.right().get(97));
        assertEquals(new Line(99, "    ", "+"), c.right().get(98));
        assertEquals(new Line(100, "    public static Collection<SearchShard> getSlaveShards(Applications mask, SolrCoreType type, Integer dbShardIndex) {", "+"), c.right().get(99));
        assertEquals(new Line(101, "        return filterByTypeApplication(mask, type, false,dbShardIndex);", "+"), c.right().get(100));
        assertEquals(new Line(102, "    }", "+"), c.right().get(101));
        assertEquals(new Line(103, ""), c.right().get(102));
        assertEquals(new Line(104, "}"), c.right().get(103));
    }

    @Test
    public void testComparison() {
        List<Line> base = new LinkedList<Line>();
        base.add(new Line(1, "1"));
        base.add(new Line(2, "2"));
        base.add(new Line(3, "3"));
        base.add(new Line(4, "4"));
        base.add(new Line(5, "5"));
        base.add(new Line(6, "6"));
        base.add(new Line(7, "7"));
        base.add(new Line(8, "8"));
        base.add(new Line(9, "9"));
        base.add(new Line(10, "10"));
        base.add(new Line(11, "11"));
        base.add(new Line(12, "12"));
        base.add(new Line(13, "13"));
        base.add(new Line(14, "14"));
        base.add(new Line(15, "15"));

        List<Line> d1 = new LinkedList<Line>();
        d1.add(new Line("2"));
        d1.add(new Line("s", "+"));
        d1.add(new Line("ss", "+"));
        d1.add(new Line("3"));

        List<Line> d2 = new LinkedList<Line>();
        d2.add(new Line("5"));
        d2.add(new Line("6", "-"));
        d2.add(new Line("7"));

        List<Line> d3 = new LinkedList<Line>();
        d3.add(new Line("9"));
        d3.add(new Line("10", "-"));
        d3.add(new Line("f", "+"));
        d3.add(new Line("11"));
        d3.add(new Line("12"));
        d3.add(new Line("13"));

        Diff.Entry e1 = new Diff.Entry(2, d1);
        Diff.Entry e2 = new Diff.Entry(5, d2);
        Diff.Entry e3 = new Diff.Entry(9, d3);

        Diff diff = new Diff(asList(e1, e2, e3));

        Comparison c = new Comparison(base, diff);

        assertEquals(17, c.left().size());

        assertEquals(new Line(1, "1"), c.left().get(0));
        assertEquals(new Line(2, "2"), c.left().get(1));
        assertEquals(new Line(3, "", "+"), c.left().get(2));
        assertEquals(new Line(4, "", "+"), c.left().get(3));
        assertEquals(new Line(5, "3"), c.left().get(4));
        assertEquals(new Line(6, "4"), c.left().get(5));
        assertEquals(new Line(7, "5"), c.left().get(6));
        assertEquals(new Line(8, "6", "-"), c.left().get(7));
        assertEquals(new Line(9, "7"), c.left().get(8));
        assertEquals(new Line(10, "8"), c.left().get(9));
        assertEquals(new Line(11, "9"), c.left().get(10));
        assertEquals(new Line(12, "|$!|10|!$|", "!"), c.left().get(11));
        assertEquals(new Line(13, "11"), c.left().get(12));
        assertEquals(new Line(14, "12"), c.left().get(13));
        assertEquals(new Line(15, "13"), c.left().get(14));
        assertEquals(new Line(16, "14"), c.left().get(15));
        assertEquals(new Line(17, "15"), c.left().get(16));

        assertEquals(17, c.right().size());
        assertEquals(new Line(1, "1"), c.right().get(0));
        assertEquals(new Line(2, "2"), c.right().get(1));
        assertEquals(new Line(3, "s", "+"), c.right().get(2));
        assertEquals(new Line(4, "ss", "+"), c.right().get(3));
        assertEquals(new Line(5, "3"), c.right().get(4));
        assertEquals(new Line(6, "4"), c.right().get(5));
        assertEquals(new Line(7, "5"), c.right().get(6));
        assertEquals(new Line(8, "", "-"), c.right().get(7));
        assertEquals(new Line(9, "7"), c.right().get(8));
        assertEquals(new Line(10, "8"), c.right().get(9));
        assertEquals(new Line(11, "9"), c.right().get(10));
        assertEquals(new Line(12, "|$!|f|!$|", "!"), c.right().get(11));
        assertEquals(new Line(13, "11"), c.right().get(12));
        assertEquals(new Line(14, "12"), c.left().get(13));
        assertEquals(new Line(15, "13"), c.left().get(14));
        assertEquals(new Line(16, "14"), c.left().get(15));
        assertEquals(new Line(17, "15"), c.left().get(16));
    }

    private List<Line> toLines(String s) throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(s));
        List<Line> base = new LinkedList<Line>();
        int i = 1;
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            base.add(new Line(i++, line));
        }
        return base;
    }

}