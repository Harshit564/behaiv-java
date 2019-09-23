/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package de.dmi3y.behaiv;

import de.dmi3y.behaiv.kernel.Kernel;
import de.dmi3y.behaiv.kernel.LogisticRegressionKernel;
import de.dmi3y.behaiv.provider.Provider;
import de.dmi3y.behaiv.provider.ProviderCallback;
import de.dmi3y.behaiv.session.CaptureSession;
import de.dmi3y.behaiv.storage.BehaivStorage;
import de.dmi3y.behaiv.tools.Pair;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.ReplaySubject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Behaiv implements ProviderCallback {

    private static ReplaySubject<String> subject;
    private Kernel kernel;
    private BehaivStorage storage;
    private List<Provider> providers;
    private CaptureSession currentSession;
    private boolean predict = true;

    private Behaiv() {
        providers = new ArrayList<>();

    }

    @Deprecated
    public synchronized static Behaiv with(@Nonnull Kernel kernel) {
        Behaiv behaiv = new Behaiv();
        behaiv.kernel = kernel;
        subject = ReplaySubject.create();
        return behaiv;

    }

    public synchronized static Behaiv with(@Nonnull String id) {
        Behaiv behaiv = new Behaiv();
        behaiv.kernel = new LogisticRegressionKernel(id);
        subject = ReplaySubject.create();
        return behaiv;
    }

    public Behaiv setKernelId(@Nonnull String id) {
        this.kernel.setId(id);
        return this;
    }

    public Behaiv setThreshold(long amount) {
        this.kernel.setTreshold(amount);
        return this;
    }

    public Behaiv setKernel(@Nonnull Kernel kernel) {
        this.kernel = kernel;
        return this;
    }

    public Behaiv setProvider(@Nonnull Provider provider) {
        providers.add(provider);
        return this;
    }

    public Behaiv setStorage(BehaivStorage storage) {
        this.storage = storage;
        return this;
    }

    public void registerLabel(@Nullable String label) {
        currentSession.captureLabel(label);
    }

    public Observable<String> subscribe() {
        return subject;
    }

    public void startCapturing(boolean predict) {
        this.predict = predict;
        if (storage != null && kernel.isEmpty()) {
            try {
                kernel.restore(storage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        currentSession = new CaptureSession(providers);
        currentSession.start(this);
    }

    protected CaptureSession getCurrentSession() {
        return currentSession;
    }

    @Override
    public void onFeaturesCaptured(List<Pair<Double, String>> features) {
        if (kernel.readyToPredict() && predict) {
            subject.onNext(kernel.predictOne(features.stream().map(Pair::getKey).collect(Collectors.toCollection(ArrayList::new))));
        }
    }

    public void stopCapturing(boolean discard) {
        if (discard) {
            currentSession = null;
            return;
        }
        String label = currentSession.getLabel();
        List<Pair<Double, String>> features = currentSession.getFeatures();
        kernel.updateSingle(features.stream().map(Pair::getKey).collect(Collectors.toCollection(ArrayList::new)), label);
        if (kernel.readyToPredict()) {
            kernel.fit();
        }
        if (storage != null) {
            try {
                kernel.save(storage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public static class Builder {

        private final Behaiv behaiv;
        private String id;

        public Builder(String id) {
            behaiv = Behaiv.with(id);
            behaiv.setKernelId(id);
            this.id = id;
        }

        protected Builder setKernel(@Nonnull Kernel kernel) {
            return setKernel(kernel, false);
        }

        protected Builder setKernel(@Nonnull Kernel kernel, boolean keepId) {
            if (!keepId) {
                kernel.setId(id);
            }
            behaiv.kernel = kernel;
            return this;
        }

        public Builder setKernelId(@Nonnull String id) {
            behaiv.setKernelId(id);
            return this;
        }

        public Builder setThreshold(long amount) {
            behaiv.setThreshold(amount);
            return this;
        }

        public Builder setProvider(@Nonnull Provider provider) {
            behaiv.providers.add(provider);
            return this;
        }

        public Builder setStorage(BehaivStorage storage) {

            behaiv.storage = storage;
            return this;
        }

        public Behaiv build() {
            return behaiv;
        }


    }


}
