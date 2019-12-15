package dupd.hku.com.hkusap.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class SPPlateModelAdapter implements JsonDeserializer<SPPlateModel> {


    @Override
    public SPPlateModel deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        SPPlateModel model = new SPPlateModel();
        JsonObject jobject = json.getAsJsonObject();

        model.plateID = jobject.get("uuid").getAsString();
        model.mPower = (double)-55.86;//jobject.get("mPower").getAsDouble();
        model.type = IEnum.SPWeightType.init(jobject.get("weight").getAsInt());
        model.name = jobject.get("searchDisplayName").getAsString();
        model.speakOut = jobject.get("speakOut").getAsString().trim();
        model.searchable = jobject.get("searchable").getAsInt();

        model.init();

        return model;
    }
}
