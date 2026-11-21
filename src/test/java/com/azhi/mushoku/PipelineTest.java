package com.azhi.mushoku;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PipelineTest {
    @Test
    void discoversJupiterTests() {
        assertEquals(2, List.of("water", "earth").size());
    }
}
