package dupd.hku.com.hkusap.persistence.api;

import java.util.List;

import dupd.hku.com.hkusap.model.Event;
import dupd.hku.com.hkusap.model.Sapdb;
import io.reactivex.Flowable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Api {

    @GET("intv2/getdbversion.php")
    Flowable<String> getdbversion();

    @GET("intv2/SAPDB.json")
    Flowable<Sapdb> sapdb();


    @GET("intv2/querytimetable.php")
    Flowable<List<Event>> querytimetable(@Query("roomname") String roomname);
}
