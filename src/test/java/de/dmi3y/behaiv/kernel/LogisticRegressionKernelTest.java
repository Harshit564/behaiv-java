package de.dmi3y.behaiv.kernel;

import org.apache.commons.math3.util.Pair;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static de.dmi3y.behaiv.kernel.KernelTest.HOME;
import static de.dmi3y.behaiv.kernel.KernelTest.WORK;
import static org.junit.Assert.assertEquals;

public class LogisticRegressionKernelTest {


    @Test
    public void predictOne() {
        ArrayList<Pair<ArrayList<Double>, String>> data = KernelTest.getTrainingData();
        Kernel dummyKernel = new LogisticRegressionKernel();
        dummyKernel.fit(data);
        ArrayList<Double> predictList = new ArrayList<>();
        predictList.add((10 * 60 + 10.0) / (24 * 60));
        predictList.add(WORK[0]);
        predictList.add(WORK[1]);
        predictList.add(1.0);

        dummyKernel.update(null);
        String prediction = dummyKernel.predictOne(predictList);
        assertEquals("WORK_SCREEN", prediction);

        predictList = new ArrayList<>();
        predictList.add((5 * 60 + 10.0) / (24 * 60));
        predictList.add(HOME[0]);
        predictList.add(HOME[1]);
        predictList.add(0.0);
        prediction = dummyKernel.predictOne(predictList);
        assertEquals("SELFIMPROVEMENT_SCREEN", prediction);
    }

    @Test
    public void storeResults() throws IOException, ClassNotFoundException {
        ArrayList<Pair<ArrayList<Double>, String>> data = KernelTest.getTrainingData();
        Kernel kernel = new LogisticRegressionKernel();
        kernel.fit(data);
        ArrayList<Double> predictList = new ArrayList<>();
        predictList.add((10 * 60 + 10.0) / (24 * 60));
        predictList.add(WORK[0]);
        predictList.add(WORK[1]);
        predictList.add(1.0);

        kernel.update(null);
        String prediction = kernel.predictOne(predictList);
        assertEquals("WORK_SCREEN", prediction);

        File tempFile = File.createTempFile("behaiv_", "test");
        File metadataFile = File.createTempFile("behaiv_", "metadataa");
        kernel.save(tempFile, metadataFile);

        kernel = new LogisticRegressionKernel();
        kernel.restore(tempFile, metadataFile);
        prediction = kernel.predictOne(predictList);
        assertEquals("WORK_SCREEN", prediction);

    }
}