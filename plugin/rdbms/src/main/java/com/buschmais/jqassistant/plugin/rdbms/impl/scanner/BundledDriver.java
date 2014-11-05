package com.buschmais.jqassistant.plugin.rdbms.impl.scanner;

import schemacrawler.schemacrawler.IncludeAll;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.tools.options.InfoLevel;

public enum BundledDriver {

    Sybaseiq {
        @Override
        public SchemaCrawlerOptions getOptions(InfoLevel level) {
            return new schemacrawler.tools.sybaseiq.BundledDriverOptions().getSchemaCrawlerOptions(level);
        }
    },
    Sqlserver {
        @Override
        public SchemaCrawlerOptions getOptions(InfoLevel level) {
            return new schemacrawler.tools.sqlserver.BundledDriverOptions().getSchemaCrawlerOptions(level);
        }
    },
    Sqlite {
        @Override
        public SchemaCrawlerOptions getOptions(InfoLevel level) {
            return new schemacrawler.tools.sqlite.BundledDriverOptions().getSchemaCrawlerOptions(level);
        }
    },
    Postgresql {
        @Override
        public SchemaCrawlerOptions getOptions(InfoLevel level) {
            return new schemacrawler.tools.postgresql.BundledDriverOptions().getSchemaCrawlerOptions(level);
        }
    },
    Oracle {
        @Override
        public SchemaCrawlerOptions getOptions(InfoLevel level) {
            SchemaCrawlerOptions options = new schemacrawler.tools.oracle.BundledDriverOptions().getSchemaCrawlerOptions(level);
            options.setSequenceInclusionRule(new IncludeAll());
            return options;
        }
    },
    Mysql {
        @Override
        public SchemaCrawlerOptions getOptions(InfoLevel level) {
            return new schemacrawler.tools.mysql.BundledDriverOptions().getSchemaCrawlerOptions(level);
        }
    },
    Hsqldb {
        @Override
        public SchemaCrawlerOptions getOptions(InfoLevel level) {
            SchemaCrawlerOptions options = new schemacrawler.tools.hsqldb.BundledDriverOptions().getSchemaCrawlerOptions(level);
            options.setSequenceInclusionRule(new IncludeAll());
            return options;
        }
    },
    Derby {
        @Override
        public SchemaCrawlerOptions getOptions(InfoLevel level) {
            return new schemacrawler.tools.derby.BundledDriverOptions().getSchemaCrawlerOptions(level);
        }
    },
    Db2 {
        @Override
        public SchemaCrawlerOptions getOptions(InfoLevel level) {
            return new schemacrawler.tools.db2.BundledDriverOptions().getSchemaCrawlerOptions(level);
        }
    };

    public abstract SchemaCrawlerOptions getOptions(InfoLevel level);
}
