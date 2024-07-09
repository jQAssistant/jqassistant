package com.buschmais.jqassistant.commandline.test;

import com.buschmais.jqassistant.core.store.impl.RemoteGraphStore;
import com.buschmais.xo.api.Query;
import com.buschmais.xo.api.Query.Result.CompositeRowObject;

import static org.assertj.core.api.Assertions.assertThat;

public class ServerIT extends AbstractCLIIT {

    @DistributionTest
    void embeddedServer() {
        withStore(store -> {
            assertThat(store).isInstanceOf(RemoteGraphStore.class);
            try (Query.Result<CompositeRowObject> result = store.executeQuery("MATCH (n) RETURN count(n) as count")) {
                assertThat(result.hasResult()).isTrue();
            }
        });
    }

}
