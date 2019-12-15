package dupd.hku.com.hkusap.weight;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.maps.model.LatLng;

import org.altbeacon.beacon.Beacon;

import java.util.List;
import java.util.Locale;

import dupd.hku.com.hkusap.HKUApplication;
import dupd.hku.com.hkusap.R;
import dupd.hku.com.hkusap.manager.DataIOManager;
import dupd.hku.com.hkusap.manager.DeadReckoningManager;
import dupd.hku.com.hkusap.manager.DeadReckoningManager.DeadReckoningProtocol;
import dupd.hku.com.hkusap.manager.MapIOManager;
import dupd.hku.com.hkusap.manager.MapIOManager.MapIOManagerObserverableProtocol;
import dupd.hku.com.hkusap.manager.RangingManager;
import dupd.hku.com.hkusap.manager.RangingManager.RangingManagerProtocol;
import dupd.hku.com.hkusap.manager.SPTeleprompter;
import dupd.hku.com.hkusap.manager.SPTeleprompterGroup;
import dupd.hku.com.hkusap.model.IDMatchingModel;
import dupd.hku.com.hkusap.model.IDPointModel;
import dupd.hku.com.hkusap.model.IDRouteModel;
import dupd.hku.com.hkusap.model.IEnum.SPWeightType;
import dupd.hku.com.hkusap.model.POIPoint;
import dupd.hku.com.hkusap.model.SPPlateModel;
import dupd.hku.com.hkusap.utils.Utils;

import static dupd.hku.com.hkusap.model.IEnum.IDPointPosition.IDPointPositionMyLocation;
import static dupd.hku.com.hkusap.model.IEnum.SPWeightType.SPWeightTypeLift;
import static dupd.hku.com.hkusap.model.IEnum.SPWeightType.SPWeightTypeOthers;

public class NavigationToolbar extends CompatToolbar implements DeadReckoningProtocol, RangingManagerProtocol, MapIOManagerObserverableProtocol {

    private ImageView mIvNavigate;
    private TextView mTvNavigate;
    private IDRouteModel mCurrentRoute;
    private SPTeleprompter mSPTeleprompter;
    private double mHeading;
    private IDMatchingModel currentPosition;
    private TextToSpeech speechSynthesizer;
    private SPTeleprompterGroup currentGroup;
    private boolean approachAnounced;
    private SPTeleprompterGroup theNextGroup;
    private boolean inLift=false;

    public NavigationToolbar(@NonNull Context context) {
        this(context, null);
    }

    public NavigationToolbar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NavigationToolbar(@NonNull final Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        View view = LayoutInflater.from(context).inflate(R.layout.toolabr_navigation, this, false);
        addView(view);
        mIvNavigate = findViewById(R.id.iv_navigate);
        mTvNavigate = findViewById(R.id.tv_navigate);
    }

    public void initialize(List<IDRouteModel> routes, IDMatchingModel position, float updatedHeading) {
        mSPTeleprompter = new SPTeleprompter(routes);
        currentPosition = position;
        mHeading = updatedHeading;


        IDPointModel point = new IDPointModel(currentPosition.coordinate, SPWeightTypeOthers, currentPosition.levelCode, IDPointPositionMyLocation);
        mCurrentRoute = mSPTeleprompter.routeForPoint(point);
        currentGroup = mSPTeleprompter.groupForRoute(mCurrentRoute);
        updateContentWithHeading(mHeading);
        String word = "Go straight forward";
        String imageName = mSPTeleprompter.imageNameForHeadingDifference(0) + "_w";
        int drawable = Utils.getResourcesDrawable(imageName);
        mTvNavigate.setText(word);
        mIvNavigate.setImageResource(drawable);

        DeadReckoningManager.getInstance().addNotifyObserver(this);
        RangingManager.getInstance().addNotifyObserver(this);
        MapIOManager.getInstance().addNotifyObserver(this);
    }


    public void updateContentWithHeading(double trueHeading) {
        /*if (mCurrentRoute == null) return;
        if (mHeading == trueHeading) return;

        double routeHeading = DeadReckoningManager.getInstance().headingFrom(mCurrentRoute.start.coordinate, mCurrentRoute.end.coordinate);
        double difference = trueHeading - routeHeading;
        String wording = mSPTeleprompter.headingDifferenceDecription(difference);
        String imageName = mSPTeleprompter.imageNameForHeadingDifference(difference) + "_w";
        int drawable = Utils.getResourcesDrawable(imageName);

        mTvNavigate.setText(wording);
        mIvNavigate.setImageResource(drawable);
        */
    }

    public String prompt(double difference) {
        if (currentGroup == null)
            return null;
        String word = "";
        String name = "";
        switch (currentGroup.type) {
            case SPWeightTypeOthers:
                word = "Go straight forward";
                word = checkNextGroup(word, difference);
                break;
            case SPWeightTypeDoor:
                word = "Go straight to the room";
                word = checkNextGroup(word, difference);
                break;
            case SPWeightTypeEntrance:
                word = "Go straight to the entrance";
                word = checkNextGroup(word, difference);
                break;
            case SPWeightTypeEscalator:
                word = "Take the Escalator to ";
                name = DataIOManager.getInstance().abbreviationForLevelCode(currentGroup.lastRoute().end.levelCode);
                word += name;
                break;
        }
        return word;
    }

    public String checkNextGroup(String word, double difference) {
        if (theNextGroup == null)
            return word;
        switch (theNextGroup.type) {
            case SPWeightTypeLift:
                word += " then take the lift";
                if(mSPTeleprompter.nextGroup(theNextGroup)!=null&&mSPTeleprompter.nextGroup(theNextGroup).groupInLift())
                    word=word+" to "+DataIOManager.getInstance().abbreviationForLevelCode(mSPTeleprompter.nextGroup(theNextGroup).lastRoute().end.levelCode);
                break;
            case SPWeightTypeEscalator:
                word += " then take the escalator";
                break;
            default:
                if (Math.abs(difference) > 30)
                    word = word + " and " + mSPTeleprompter.headingDifferenceDecription(difference).toLowerCase();
                break;
        }
        return word;
    }

    @Override
    public void didCalculatedDeadReckoningModel(IDMatchingModel model, IDRouteModel route) {
        mCurrentRoute = route;
        currentPosition = model;

        String word = "Go straight forward";
        double difference = 0;
        SPTeleprompterGroup group = mSPTeleprompter.groupForRoute(route);
        if (currentGroup != group) {
            currentGroup = group;
            if (currentGroup == null)
                return;
            theNextGroup = mSPTeleprompter.nextGroup(group);
            approachAnounced = false;

            double routeHeading = 0;
            routeHeading = DeadReckoningManager.getInstance().headingFrom(mCurrentRoute.start.coordinate, mCurrentRoute.end.coordinate);

            if (theNextGroup != null) {
                double theNextGroupHeading = DeadReckoningManager.getInstance().headingFrom(theNextGroup.routes.get(0).start.coordinate,
                        theNextGroup.routes.get(0).end.coordinate);
                difference = routeHeading - theNextGroupHeading;
            }
            word = prompt(difference);
            //speechSentence(turningInformationForRoute(route));
        } else {
            if (currentGroup == null) {
                return;
            }
            if (currentGroup.distanceLeftFromCoordinate(model.coordinate, route) < 5 && !approachAnounced) {
                SPTeleprompterGroup nextGroup = mSPTeleprompter.nextGroup(group);
                if (nextGroup != null) {
                    approachAnounced = true;
                    String wording = String.format("%s later", turningInformationForRoute(nextGroup.firstRoute()));
                    //speechSentence(wording);
                }
            }
        }

        String imageName = mSPTeleprompter.imageNameForHeadingDifference(difference) + "_w";
        int drawable = Utils.getResourcesDrawable(imageName);
        if(word!=null&&word!="") {
            mTvNavigate.setText(word);
            mIvNavigate.setImageResource(drawable);
        }
    }

    public String turningInformationForRoute(IDRouteModel route) {
        double routeHeading = DeadReckoningManager.getInstance().headingFrom(route.start.coordinate, route.end.coordinate);
        double difference = mHeading - routeHeading;
        String wording = mSPTeleprompter.headingDifferenceDecription(difference);
        return wording;
    }

    @Override
    public void didArrivedAtPlate(SPPlateModel plate) {
        if (TextUtils.isEmpty(plate.speakOut)) {
            return;
        }
        speechSentence(String.format("Arrived at %s", plate.speakOut));
    }

    @Override
    public void didArrivedDestination(SPPlateModel plate) {
        speechSentence(String.format("Destination arrived, %s", plate.name));
    }

    @Override
    public void didUpdatedHeading(float heading) {
        updateContentWithHeading(heading);
    }

    @Override
    public void didRangedNearestBeacon(Beacon beacon) {
        SPPlateModel model = RangingManager.getInstance().plateForBeacon(beacon);
        if (model == null) return;


        String statusImageName;
        if (model.levelCode.equals("000") && model.type == SPWeightTypeLift) {
            statusImageName = "liftin_l_w";
            int drawable = Utils.getResourcesDrawable(statusImageName);
            mIvNavigate.setImageResource(drawable);

            if(!inLift) {
                LatLng coordinate = model.getCoordinate();
                SPTeleprompterGroup liftGroups = mSPTeleprompter.nearestLiftGroup(coordinate);
                if (liftGroups != null) {
                    String name = DataIOManager.getInstance().abbreviationForLevelCode(liftGroups.lastRoute().end.levelCode);
                    String word = "Take the lift to " + name;
                    mTvNavigate.setText(word);
                    inLift = true;
                }
            }
        }else inLift=false;

        statusImageName = imageWithType(model.type);
        int drawable = Utils.getResourcesDrawable(statusImageName);

        mIvNavigate.setImageResource(drawable);
    }

    private String imageWithType(SPWeightType type) {
        switch (type) {
            case SPWeightTypeDoor:
                return "door_l_w";
            case SPWeightTypeLift:
                return "lift_l_w";
            case SPWeightTypeEntrance:
                return "entrance_l_w";
            case SPWeightTypeEscalator:
                return "escalator_l_w";
            case SPWeightTypeOthers:
            default:
                return null;
        }
    }

    public void speechSentence(String wording) {
        if (TextUtils.isEmpty(wording)) {
            return;
        }
        if (speechSynthesizer == null) {
            speechSynthesizer = new TextToSpeech(HKUApplication.sAPP, status -> {
                if (status == TextToSpeech.SUCCESS) {
                    speechSynthesizer.setPitch(1.0f);
                    speechSynthesizer.setSpeechRate(1.0f);
                    speechSynthesizer.setLanguage(Locale.ENGLISH);
                } else {
                    DataIOManager.getInstance().mSubject.onNext("不支持语音播报 -- TextToSpeech\n");
                }
            });
        }
        DataIOManager.getInstance().mSubject.onNext("播报->" + wording + "\n");
        speechSynthesizer.speak(wording, TextToSpeech.QUEUE_ADD, null);
    }

    public void didRangedBeacons(List<Beacon> beacons) {
        RangingManagerProtocol.super.didRangedBeacons(beacons);
    }

}
