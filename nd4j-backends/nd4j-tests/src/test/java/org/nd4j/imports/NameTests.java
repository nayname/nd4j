package org.nd4j.imports;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.nd4j.autodiff.samediff.SameDiff;
import org.nd4j.linalg.BaseNd4jTest;
import org.nd4j.linalg.factory.Nd4jBackend;

@Slf4j
@RunWith(Parameterized.class)
public class NameTests  extends BaseNd4jTest {

    public NameTests(Nd4jBackend backend) {
        super(backend);
    }

    @Test
    public void testNameExtraction_1() throws Exception {
        val str = "Name";
        val exp = "Name";

        val pair = SameDiff.parseVariable(str);
    }

    @Override
    public char ordering() {
        return 'c';
    }
}
