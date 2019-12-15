package dupd.hku.com.hkusap.utils;

import java.util.List;

import dupd.hku.com.hkusap.manager.DataIOManager;
import dupd.hku.com.hkusap.model.Event;
import dupd.hku.com.hkusap.model.SPPlateModel;
import dupd.hku.com.hkusap.model.SPRoomModel;
import dupd.hku.com.hkusap.persistence.DailyyogaClient;
import dupd.hku.com.hkusap.persistence.api.Api;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class ApiUtil {

    private final Api mApi;

    private ApiUtil() {
        mApi = DailyyogaClient.retrofit().create(Api.class);
    }

    private static ApiUtil INSTANCE;

    public static ApiUtil getInstance() {
        if (INSTANCE == null) INSTANCE = new ApiUtil();
        return INSTANCE;
    }

    public void sapdb() {
        getdbversion();
        mApi.sapdb()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(sapdb -> {
                    AssetsUtil.writeSapdb(sapdb);
                    //DataIOManager.getInstance().sapdb = sapdb;
                }, Throwable::printStackTrace)
                .isDisposed();
    }


    private void getdbversion() {
        mApi.getdbversion()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {

                }, Throwable::printStackTrace)
                .isDisposed();
    }

    public void querytimetable(SPPlateModel plateModel, Consumer<List<Event>> consumer) {
        if (plateModel == null) {
            try {
                consumer.accept(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        SPRoomModel room = DataIOManager.getInstance().plateToRoom(plateModel);
        if (room == null) {
            try {
                consumer.accept(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        mApi.querytimetable(room.name)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(consumer, throwable -> {
                    throwable.printStackTrace();
                    consumer.accept(null);
                })
                .isDisposed();
    }
}
