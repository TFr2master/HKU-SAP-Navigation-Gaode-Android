package dupd.hku.com.hkusap.manager;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.app.Application;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;

import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.AMap;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.GroundOverlay;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polygon;
import com.amap.api.maps.model.PolygonOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;


import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import dupd.hku.com.hkusap.HKUApplication;
import dupd.hku.com.hkusap.R;
import dupd.hku.com.hkusap.manager.CLLocationManager.CLLocationManagerDelegate;
import dupd.hku.com.hkusap.manager.DeadReckoningManager.DeadReckoningProtocol;
import dupd.hku.com.hkusap.manager.RangingManager.RangingManagerProtocol;
import dupd.hku.com.hkusap.model.IDFloorPlan;
import dupd.hku.com.hkusap.model.IDMatchingModel;
import dupd.hku.com.hkusap.model.IDPathResultModel;
import dupd.hku.com.hkusap.model.IDPointModel;
import dupd.hku.com.hkusap.model.IDRouteModel;
import dupd.hku.com.hkusap.model.IEnum.SPSelectedIndexType;
import dupd.hku.com.hkusap.model.IEnum.SPUserLocationButtonType;
import dupd.hku.com.hkusap.model.Often;
import dupd.hku.com.hkusap.model.POIPoint;
import dupd.hku.com.hkusap.model.SPBuildingBoundary;
import dupd.hku.com.hkusap.model.SPPlateModel;
import dupd.hku.com.hkusap.model.SPSelectedIndex;
import dupd.hku.com.hkusap.model.Sapdb.LevelCodeComparator;
import dupd.hku.com.hkusap.utils.AnimatorUtil;
import dupd.hku.com.hkusap.utils.AssetsUtil;
import dupd.hku.com.hkusap.utils.GeoUtils;
import dupd.hku.com.hkusap.utils.UIColor;
import dupd.hku.com.hkusap.weight.BottomSheet;
import dupd.hku.com.hkusap.weight.BottomSheetNavigation;
import dupd.hku.com.hkusap.weight.BottomSheetRoute;
import dupd.hku.com.hkusap.weight.BottomSheetState;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static dupd.hku.com.hkusap.manager.MapManagerMode.MapManagerModeNavigation;
import static dupd.hku.com.hkusap.manager.MapManagerMode.MapManagerModePositioning;
import static dupd.hku.com.hkusap.manager.MapManagerMode.MapManagerModeRouteFinding;
import static dupd.hku.com.hkusap.model.IEnum.IDPointPosition.IDPointPositionDestination;
import static dupd.hku.com.hkusap.model.IEnum.IDPointPosition.IDPointPositionMyLocation;
import static dupd.hku.com.hkusap.model.IEnum.SPSelectedIndexType.SPSelectedIndexTypeFrom;
import static dupd.hku.com.hkusap.model.IEnum.SPSelectedIndexType.SPSelectedIndexTypeTo;
import static dupd.hku.com.hkusap.model.IEnum.SPSelectedIndexType.SPSelectedIndexTypeTransition;
import static dupd.hku.com.hkusap.model.IEnum.SPUserLocationButtonType.SPUserLocationButtonHightlighted;
import static dupd.hku.com.hkusap.model.IEnum.SPUserLocationButtonType.SPUserLocationButtonTracking;
import static dupd.hku.com.hkusap.model.IEnum.SPWeightType.SPWeightTypeOthers;

public class MapIOManager implements RangingManagerProtocol, DeadReckoningProtocol, CLLocationManagerDelegate {
    private static final float MaxZoomLevel = 21f;
    private static MapIOManager INSTANCE;
    private final CLLocationManager locationManager;
    public AMap mMap;
    private IDPointModel fromPoint;
    private IDPointModel toPoint;
    private IDRouteCollection routeCollection;
    public IDRouteCollection allRoute=null;
    private ViewController mController;
    private Location mLocation;
    public MapManagerMode mode;
    private IDMatchingModel myLocationModel;
    private float updatedHeading;
    public String level;
    private boolean liftInside;
    public String previewLevel;
    private List<String> levels = new ArrayList<>();
    private SPPlateModel selectedPlate;
    private boolean isReady;
    private List<MapIOManagerObserverableProtocol> observerArray = new ArrayList<>();
    private IDPathResultModel pathResult;
    private List<Polyline> enabledLines = new ArrayList<>();
    private List<Polyline> disabledLines = new ArrayList<>();
    private List<SPSelectedIndex> selectedLevelIndex;
    private Marker myLocationMarker;
    private Marker fromMarker;
    private Marker toMarker;
    private IDFloorPlan floorPlan;
    private boolean arrivedDestination;
    private GroundOverlay mGroundOverlay;
    private List<Polygon> buildingBoundaries = new ArrayList<>();
    private boolean centerInMap;
    private boolean navigationTrackingEnabled;
    private Beacon nearestBeacon;
    public int oldZoomLevel=0;
    private List<Marker> POIMarker = new ArrayList<>();
    public static boolean useCheck=true;
    public boolean showRoute=false;

    private MapIOManager() {
        mode = MapManagerModePositioning;
        locationManager = new CLLocationManager();
        locationManager.delegate = this;
    }

    public static MapIOManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MapIOManager();
        }
        return INSTANCE;
    }

    public void dealloc() {
        level = "";//这句很重要;
        RangingManager.getInstance().removeNotifyObserver(this);
        DeadReckoningManager.getInstance().removeNotifyObserver(this);
    }

    public void registerViewController(ViewController controller, AMap aMap) {
        mController = controller;
        mMap = aMap;
    }

    public List<String> registeredLevels() {
        return levels;
    }

    public void setupInitialDisplay() {
        navigationTrackingEnabled = true;
        RangingManager.getInstance().addNotifyObserver(this);
        DeadReckoningManager.getInstance().addNotifyObserver(this);

        setupLevels();
        loadURLQuery();
        setupSignalTimer();
    }

    public SPPlateModel destinationPlate() {
        return selectedPlate;
    }

    public void setupLevels() {
        try {
            levels.clear();
            String json = AssetsUtil.readAssetJson("Boundary/boundary.json");
            JSONArray array = new JSONObject(json).optJSONArray("levels");
            for (int i = 0; i < array.length(); i++) {
                levels.add(array.optString(i));
            }
            setLevel("100");
            isReady = true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setupSignalTimer() {
//        @weakify(self);
//
//        _warningManager = [[BLTNItemManager alloc] initWithRootItem:[BulletinBoardDataSource makeWeakSignalPage]];
//
//    [[[RACSignal interval:10 onScheduler:[RACScheduler mainThreadScheduler]] take:1] subscribeNext:^(id x) {
//        @strongify(self);
//        if (self.myLocationModel || self.mode == MapManagerModePositioning)
//        {
//            return;
//        }
//
//        [self.warningManager showBulletinAboveViewController:self.controller animated:YES completion:nil];
//    }];
    }

    public void setSelectedPlate(SPPlateModel selectedPlate) {
        this.selectedPlate = selectedPlate;
        for (MapIOManagerObserverableProtocol observer : observerArray) {
            observer.didUpdateDestinationPlate(selectedPlate);
        }
    }

    public void updateDestinationPlateAlone(SPPlateModel plate, boolean zoom, Runnable finish) {
        selectedPlate = plate;
        removeDestination();
        if (plate != null) {
            addDestinationZoomIn(zoom, finish);
        }
    }


    public void updateMapManagerMode(MapManagerMode toMode, boolean force, Runnable completion) {
        if (mode == toMode) {
            if (!force) return;
        }
        switch (mode) {
            case MapManagerModeNavigation:
                if (toMode == MapManagerModeRouteFinding) {
                    mode = toMode;
                    AnimatorUtil.getInstance().hideNavigationShowRouteFinding();
                    float height = BottomSheetState.getInstance().mBottomSheetNavigation.mState.height;
                    BottomSheetState.getInstance().stateNavigation(BottomSheetNavigation.State.STATE_HIDDEN, selectedPlate, pathResult, () ->
                            BottomSheetState.getInstance().stateRouteFinding(BottomSheetRoute.State.STATE_COLLAPSED, selectedPlate, pathResult, () -> {
                                float offset = BottomSheetRoute.State.STATE_COLLAPSED.height - height;
                                mMap.moveCamera(CameraUpdateFactory.scrollBy(0, offset));
                                animateOutOfNavigationMode(null);
                            }));
                }
                break;
            case MapManagerModeRouteFinding:
                switch (toMode) {
                    case MapManagerModePositioning:
                        mode = toMode;
                        if (selectedPlate != null) {
                            AnimatorUtil.getInstance().hideRouteFindingShowPositioning();
                            BottomSheetState.getInstance().stateRouteFinding(BottomSheetRoute.State.STATE_HIDDEN, null, null, () ->
                                    BottomSheetState.getInstance().statePositioning(BottomSheet.State.STATE_COLLAPSED, selectedPlate, null));
                        } else {
                            AnimatorUtil.getInstance().hideRouteFindingShowPositioning();
                        }
                        if (completion != null) completion.run();
                        break;
                    case MapManagerModeNavigation:
                        navigationTrackingEnabled=false;
                        if (selectedPlate == null) {
                            if (completion != null) completion.run();
                            return;
                        }
                        mode = toMode;
                        animateToNavigationMode(null);

                        IDMatchingModel position;
                        if (myLocationModel != null) {
                            position = myLocationModel.clone();
                        } else {
                            LatLng coordinate = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
                            position = new IDMatchingModel(coordinate, level, false, false);
                        }

                        AnimatorUtil.getInstance().hideRouteFindingShowNavigation(pathResult.routes, position, updatedHeading);
                        BottomSheetState.getInstance().stateRouteFinding(BottomSheetRoute.State.STATE_HIDDEN, selectedPlate, pathResult, () ->
                                BottomSheetState.getInstance().stateNavigation(BottomSheetNavigation.State.STATE_EXPANDED, selectedPlate, pathResult, () -> {
                                            mMap.moveCamera(CameraUpdateFactory.zoomTo(MaxZoomLevel - 0.5f));
                                            locationManager.mOnStepUpdateListener.onStepUpdate(0);
                                        }
                                ));
                        break;
                    case MapManagerModeRouteFinding:
                        mode = toMode;
                        animateOutOfNavigationMode(null);

                        break;
                }

                break;
            case MapManagerModePositioning:
                if (toMode == MapManagerModeRouteFinding) {
                    mode = toMode;
                    if (selectedPlate != null) {
                        AnimatorUtil.getInstance().hidePositioningShowRouteFinding(selectedPlate);
                        BottomSheetState.getInstance().statePositioning(BottomSheet.State.STATE_HIDDEN, selectedPlate, () ->
                                BottomSheetState.getInstance().stateRouteFinding(BottomSheetRoute.State.STATE_COLLAPSED, selectedPlate, pathResult, () ->
                                {
                                    if (completion != null) completion.run();
                                }));
                    } else {
                        AnimatorSet animatorSet = AnimatorUtil.getInstance().hidePositioningShowRouteFinding(null);
                        animatorSet.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                if (completion != null) completion.run();
                            }
                        });
                    }
                    break;
                }
                break;
        }
    }

    public IDPointModel getStartingPoint() {
        IDPointModel from;
        if (fromPoint != null) {
            from = fromPoint;
        } else {
            LatLng coordinate = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
            from = new IDPointModel(coordinate, SPWeightTypeOthers, level, IDPointPositionMyLocation);
        }
        return from;
    }

    public IDPointModel getDestinationPoint() {
        return toPoint;
    }

    public boolean prepareForShortestPathCalculation() {
        if (toPoint == null) return false;

        if (!addStartingPoint()) {
            return false;
        }

        fromPoint = getStartingPoint();

        return true;
    }

    public IDPathResultModel getShortestPathResult() {
        IDPathFinderManager finder = IDPathFinderManager.getInstance();
        IDPathResultModel result = finder.shortestPathFrom(fromPoint, toPoint);
        return result;
    }

    public boolean displayShortestPathResult(IDPathResultModel result) {
        if (result.routes.isEmpty()) return false;

        pathResult = result;
        routeCollection = new IDRouteCollection(result.routes);
        String levelCode = previewLevel;

        removeNavigationRoutes();
        displayNavigationRoutesForLevel(levelCode);

        List<SPSelectedIndex> indexes = shortestPathLevelIndicators();

        for (MapIOManagerObserverableProtocol observer : observerArray) {
            observer.didDisplayedShortestPath(routeCollection, indexes);
        }
        return true;
    }

    public void displayNavigationRoutesForLevel(String level) {
        PolylineOptions enabled = routeCollection.enabledRathsForLevel(level);
        List<PolylineOptions> disabled = routeCollection.disabledPathsForLevel(level);

        enabled.color(UIColor.systemBlueColor())
                .width(mController.getResources().getDimension(R.dimen.dp_5));
        enabledLines.add(mMap.addPolyline(enabled));

        for (PolylineOptions options : disabled) {
            options.color(UIColor.black40Color())
                    .width(mController.getResources().getDimension(R.dimen.dp_5));
            disabledLines.add(mMap.addPolyline(options));
        }
    }

    public void displayAllRoutesForLevel() {
        if(allRoute==null)
            allRoute=new IDRouteCollection(IDPathFinderManager.getInstance().routes);
        List<IDRouteModel> lvRoute=allRoute.findForLevelCode(previewLevel);
        for(IDRouteModel route: lvRoute) {
            enabledLines.add(mMap.addPolyline(new PolylineOptions()
                    .add(route.start.coordinate, route.end.coordinate)
                    .width(mController.getResources().getDimension(R.dimen.dp_5))
                    .color(UIColor.systemBlueColor())));
        }
    }
    public void cleanShortestPath() {
        removeNavigationRoutes();
        removeStartingPoint();

        arrivedDestination = false;
        routeCollection = null;
        pathResult = null;
        selectedLevelIndex = null;

        for (MapIOManagerObserverableProtocol observer : observerArray) {
            observer.managerDidRemovedShortestPath(this);
        }
    }

    public void removeNavigationRoutes() {
        for (Polyline polyline : enabledLines) {
            polyline.remove();
        }
        enabledLines.clear();

        for (Polyline polyline : disabledLines) {
            polyline.remove();
        }
        disabledLines.clear();
    }

    public List<SPSelectedIndex> shortestPathLevelIndicators() {
        if (routeCollection == null) return null;

        if (selectedLevelIndex == null) {
            String fromLevel = routeCollection.routes.get(0).levelCode;
            String toLevel = routeCollection.routes.get(routeCollection.routes.size() - 1).levelCode;
            List<String> levelCodes = routeCollection.shouldIndicateRouteLevelCodes();

            Collections.sort(levelCodes, new LevelCodeComparator());

            List<SPSelectedIndex> results = new ArrayList<>();
            for (String code : levelCodes) {
                int index = levels.indexOf(code);
                SPSelectedIndexType type = SPSelectedIndexTypeTransition;
                if (code.equals(toLevel)) {
                    type = SPSelectedIndexTypeTo;
                } else if (code.equals(fromLevel)) {
                    type = SPSelectedIndexTypeFrom;
                }

                SPSelectedIndex object = new SPSelectedIndex(index, type);
                results.add(object);
                selectedLevelIndex = results;
            }
        }
        return selectedLevelIndex;
    }

    public void openURLPlate(SPPlateModel plate) {
        if (plate == null) {
            mController.showLoadDialog("Route not found");
            mController.dismissDialogDelay(3_000);
            return;
        }
        cleanShortestPath();
        updateDestinationPlateAlone(plate, false, null);
        DeadReckoningManager.getInstance().switchToMapMatchingMode(false, null);

        mController.showLoadDialog("Searching Route...");

        if (!prepareForShortestPathCalculation()) {
            mController.showLoadDialog("Route not found");
            mController.dismissDialogDelay(3_000);

            removeStartingPoint();
            removeDestination();
            updateDestinationPlateAlone(null, false, null);
            return;
        }

        Observable.create((ObservableOnSubscribe<IDPathResultModel>) emitter -> {
            final IDPathResultModel result = getShortestPathResult();
            if (result == null || result.routes == null || result.routes.isEmpty()) {
                emitter.onError(new RuntimeException("result == null"));
            } else {
                emitter.onNext(result);
            }
            emitter.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    mController.dismissDialogDelay(2_000);

                    displayShortestPathResult(result);
                    DeadReckoningManager.getInstance().switchToMapMatchingMode(true, result.routes);
                    updateMapManagerMode(MapManagerModeRouteFinding, true, () -> boundsToShortestPath(null));
                }, throwable -> {
                    throwable.printStackTrace();
                    mController.showLoadDialog("Route not found");
                    mController.dismissDialogDelay(3_000);

                    removeStartingPoint();
                    removeDestination();
                    updateDestinationPlateAlone(null, false, null);
                }).isDisposed();
    }

    public void openURLWithQuery(String query) {

    }

    public void loadURLQuery() {

    }

    public boolean addStartingPoint() {
        LatLng coordinate = null;
        if (myLocationModel != null) {
            coordinate = myLocationModel.coordinate;
        } else {
            if (mLocation != null) {
                coordinate = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
            }
        }

        if (coordinate == null) {
            return false;
        }

        fromPoint = new IDPointModel(coordinate, SPWeightTypeOthers, level, IDPointPositionMyLocation);

        if (fromMarker == null) {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(coordinate)
                    .zIndex(1)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_from));

            fromMarker = mMap.addMarker(markerOptions);
        } else {
            fromMarker.setPosition(fromPoint.coordinate);
        }
        return true;
    }


    public void removeStartingPoint() {
        if (fromMarker != null) fromMarker.remove();
        fromMarker = null;
        fromPoint = null;
    }


    public void addDestinationZoomIn(boolean zoom, Runnable finish) {
        if (toMarker != null) {
            removeDestination();
        }

        IDPointModel point = new IDPointModel(selectedPlate);
        point.position = IDPointPositionDestination;
        point.name = selectedPlate.name;
        toPoint = point;


        LatLng coordinate = selectedPlate.getCoordinate();

        MarkerOptions markerOptions = new MarkerOptions()
                .position(coordinate)
                .zIndex(1)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_to));


        toMarker = mMap.addMarker(markerOptions);

        if (zoom) {
            animateToCoordinate(coordinate, MaxZoomLevel - 2, finish);
        }
    }

    public void removeDestination() {
        if (toMarker != null) toMarker.remove();
        toMarker = null;
        toPoint = null;
    }

    public void stopRangingPlates() {
//        for (Region region : rangedRegions) {
//            locationManager.stopMonitoringForRegion(region);
//            locationManager.stopRangingBeaconsInRegion(region);
//        }
        locationManager.stopAllRegionBeacon();
    }

    public void startManagerMapMatching(boolean matching) {
        locationManager.startUpdatingLocation();

        boolean headingAvailable = locationManager.startUpdatingHeading();

        DataIOManager.getInstance().mSubject.onNext("方向传感器:" + (headingAvailable ? "可用" : "不可用") + "\n");

        locationManager.startAllRegionBeacon();
        /*
        mController.showLoadDialog("加载中...");
        Observable.fromIterable(DataIOManager.getInstance().sapdb.plate)
                .map(model -> {
                    UUID uuid = UUID.fromString(model.UUID);
                    Region region = new Region(model.UUID, Identifier.fromUuid(uuid), Identifier.fromInt(7275), null);
                    return region;
                })
                .filter(region -> region.getUniqueId() != null)
                .map(region -> {
                    locationManager.startRangingBeaconsInRegion(region);
                    return region;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .buffer(DataIOManager.getInstance().sapdb.plate.size())
                .subscribe(regions -> {
                    mController.dismissDialogDelay(0);
                }, Throwable::printStackTrace).isDisposed();
                */

        DeadReckoningManager.getInstance().startDeadReckoningWithMapMatchingEnabled(matching);
    }

    public void stopManager() {
        locationManager.stopUpdatingHeading();
        locationManager.stopUpdatingLocation();

        DeadReckoningManager.getInstance().stopDeadReckoning();
        RangingManager.getInstance().clear();
    }

    public void animateToNavigationMode(Runnable finish) {

        if (myLocationModel == null && mLocation == null) return;

        LatLng coordinate = myLocationModel != null ? myLocationModel.coordinate : new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        if (myLocationMarker != null) {
            myLocationMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.location_navigation));
        }
        updateBuildingBoundaryWithLevel(previewLevel);
        animateToCoordinate(coordinate, MaxZoomLevel - 1, finish);
    }

    public void animateOutOfNavigationMode(Runnable finish) {
        arrivedDestination = false;

        if (myLocationMarker != null) {
            myLocationMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.location_app));
        }
        if (myLocationModel != null) {
            animateToCoordinate(myLocationModel.coordinate, MaxZoomLevel - 3, null);
        }
        updateBuildingBoundaryWithLevel(previewLevel);
        boundsToShortestPath(finish);
    }

    private void animateToCoordinate(LatLng coordinate, Runnable finish) {
        animateToCoordinate(coordinate, mMap.getCameraPosition().zoom, finish);
    }

    private void animateToCoordinate(LatLng coordinate, float zoom, Runnable finish) {
        CameraUpdate update;
        if (mode != MapManagerModeNavigation) {
            update = CameraUpdateFactory.newLatLngZoom(coordinate, zoom);
        } else {

            CameraPosition cameraPosition;
            centerInMap = true;
            if (!navigationTrackingEnabled) {
                cameraPosition = CameraPosition.builder().target(coordinate)
                        .zoom(zoom)
                        .build();
            } else {
                cameraPosition = CameraPosition.builder()
                        .target(coordinate)
                        .zoom(zoom)
                        .bearing(updatedHeading)//方向
                        .tilt(40)
                        .build();
            }
            update = CameraUpdateFactory.newCameraPosition(cameraPosition);
        }
        mMap.animateCamera(update, new AMap.CancelableCallback() {
            @Override
            public void onFinish() {
                if (finish != null) finish.run();
            }

            @Override
            public void onCancel() {

            }
        });
    }

    private void animateToFlyover() {
        LatLngBounds.Builder bounds = new LatLngBounds.Builder()
                .include(mGroundOverlay.getBounds().southwest)
                .include(mGroundOverlay.getBounds().northeast);

        if (mLocation != null) {
            bounds.include(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()));
        }

        if (myLocationModel != null) {
            bounds.include(myLocationModel.coordinate);
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 0));
    }

    public void centerToMyLocation() {
        if (myLocationModel == null && mLocation == null) return;

        if(previewLevel!=level&&(!"000".equals(level))) {
            setPreviewLevel(level);
        }

        if (mode != MapManagerModeNavigation) {
            centerInMap = true;
            LatLng coordinate = myLocationModel != null ? myLocationModel.coordinate : new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
            animateToCoordinate(coordinate, null);
        } else {
            if (!centerInMap) {
                centerInMap = true;
                animateToCoordinate(myLocationModel.coordinate, null);
            } else {
                navigationTrackingEnabled = !navigationTrackingEnabled;
                animateToCoordinate(myLocationModel.coordinate, null);

                for (MapIOManagerObserverableProtocol delegate : observerArray) {
                    delegate.shouldChangeUserLocationButtonType(navigationTrackingEnabled ? SPUserLocationButtonTracking : SPUserLocationButtonHightlighted);
                }

            }
        }
    }

    public void centerToFloorPlan() {
        animateToCoordinate(floorPlan.centerCoordinate(), MaxZoomLevel - 3, null);
    }

    public void boundsToShortestPath(Runnable finish) {
        LatLngBounds.Builder bounds = new LatLngBounds.Builder()
                .include(fromPoint.coordinate)
                .include(toPoint.coordinate);

        for (Polyline polyline : enabledLines) {
            for (LatLng latLng : polyline.getPoints()) {
                bounds.include(latLng);
            }
        }

        for (Polyline polyline : disabledLines) {
            for (LatLng latLng : polyline.getPoints()) {
                bounds.include(latLng);
            }
        }

        Rect rect = mController.mapViewBounds();

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), rect.right, rect.bottom, 0), new AMap.CancelableCallback() {
            @Override
            public void onFinish() {
                if (finish != null) finish.run();
            }

            @Override
            public void onCancel() {

            }
        });
    }


    public void arrivalDestinationChecking() {
        if (arrivedDestination || mode != MapManagerModeNavigation) return;

        IDPointModel destination = routeCollection.destinationPoint();

        if (GeoUtils.distance(myLocationModel.coordinate, destination.coordinate) > 3) {
            return;
        }
        arrivedDestination = true;
        BottomSheetNavigation.arriveExit(destination.name);

        for (MapIOManagerObserverableProtocol observer : observerArray) {
            observer.didArrivedDestination(selectedPlate);
        }
    }

    public void exitCampusBoundChecking() {

    }

    public boolean canStartNavigation() {
        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(floorPlan.bottomLeftCoordinate())
                .include(floorPlan.topRightCoordinate()).build();

        if (fromPoint == null) {
            return bounds.contains(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()));
        }

        return bounds.contains(fromPoint.coordinate);
    }


    private void disableMyLocationMarker() {

    }

    public void updateMyLocationModel(IDMatchingModel model) {
        myLocationModel = model;
        if (myLocationMarker == null) {

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(model.coordinate)
                    .anchor(0.5F, 0.5F)
                    .zIndex(1)
                    .setFlat(true);

            myLocationMarker = mMap.addMarker(markerOptions);
        } else {
            myLocationMarker.setPosition(model.coordinate);
        }

        int imageName;
        if (liftInside) {
            imageName = mode == MapManagerModeNavigation ? R.drawable.location_navigation_d : R.drawable.location_d;
        } else {
            imageName = mode == MapManagerModeNavigation ? R.drawable.location_navigation : (model.inConfidenceRange ? R.drawable.location_sap : R.drawable.location_app);
        }

        myLocationMarker.setIcon(BitmapDescriptorFactory.fromResource(imageName));
        setLevel(model.levelCode);
        if (mode == MapManagerModeNavigation) {
            animateToCoordinate(model.coordinate, null);
        }
    }

    public void updateNearestPlateModel(SPPlateModel plate) {
        IDPointModel point = new IDPointModel(plate);
        point.position = IDPointPositionMyLocation;
        fromPoint = point;
    }


    public void updateMyLocationHeading(float heading) {
        updatedHeading = heading;

        if (mode == MapManagerModeNavigation) {
            if (myLocationModel != null) {
                myLocationMarker.setRotateAngle(heading);
            }

            LatLng coordinate;
            if (myLocationModel != null) {
                coordinate = myLocationModel.coordinate;
            } else {
                coordinate = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
            }

            centerInMap = true;
            if (coordinate == null) return;
            animateToCoordinate(coordinate, null);
        } else {
            if (myLocationModel != null) {
                //rotate marker icon
                myLocationMarker.setRotateAngle(heading);
            }
        }
    }

    public void updateLevel(SPPlateModel plate) {
        String level = plate.levelCode;
        setLevel(level);
    }

    public void setLevel(String level) {
        DeadReckoningManager.getInstance().pause = "000".equals(level);
        if (level.equals(this.level)) {
            return;
        }

        //inside lift
        if ("000".equals(level)) {
            this.level=level;
            liftInside = true;
            disableMyLocationMarker();
            for (MapIOManagerObserverableProtocol observer : observerArray) {
                observer.managerDidInsideLift(this);
            }
            return;
        }

        //not inside lift
        liftInside = false;
        this.level = level;
        previewLevel = level;

        for (MapIOManagerObserverableProtocol observer : observerArray) {
            observer.shouldChangeToLevel(level, false);
        }

        updateFloorPlanWithLevel(level);
        updateBuildingBoundaryWithLevel(level);
        updatePOIPointWithLevel(level);

        if (routeCollection != null) {
            removeNavigationRoutes();
            displayNavigationRoutesForLevel(level);
        }
    }

    public void setPreviewLevel(String previewLevel) {
        if (previewLevel.equals(this.previewLevel)) {
            return;
        }

        this.previewLevel = previewLevel;

        for (MapIOManagerObserverableProtocol observer : observerArray) {
            observer.shouldChangeToLevel(previewLevel, true);
        }

        updateFloorPlanWithLevel(previewLevel);
        updateBuildingBoundaryWithLevel(previewLevel);
        updatePOIPointWithLevel(previewLevel);
        if(showRoute){
            removeNavigationRoutes();
            displayAllRoutesForLevel();
        }

        if (routeCollection != null) {
            removeNavigationRoutes();
            displayNavigationRoutesForLevel(previewLevel);
        }
    }

    public void updatePOIPointWithLevel(String level){
        cleanPOIPointBoundary();
        addPOIPointWithLevel(level);
    }
    public void cleanPOIPointBoundary() {
        for (Marker poi : POIMarker) {
            poi.remove();
        }
        POIMarker.clear();
    }
    public void updatePOIPointWithZoom(int zoomlevel){
        for(int i=0;i<POIMarker.size();i++){
            if (((POIPoint)POIMarker.get(i).getObject()).mapScale <= zoomlevel) {
                POIMarker.get(i).setVisible(true);
            } else {
                POIMarker.get(i).setVisible(false);
            }
        }
    }
    public void addPOIPointWithLevel(String level) {
        List<POIPoint> result = DataIOManager.getInstance().POIPointForLevel(level);

        for (POIPoint object : result) {
            oldZoomLevel=2;
            int id = R.drawable.search;
            if (object.poi_type.equals("entrance")) {
                id = R.drawable.entrance;
            } else if (object.poi_type.equals("lift")) {
                id = R.drawable.lift;
            } else if (object.poi_type.equals("parking")) {
                id = R.drawable.parking;
            } else if (object.poi_type.equals("room")) {
                id = R.drawable.room;
            } else if (object.poi_type.equals("food")) {
                id = R.drawable.restaurant;
            } else if (object.poi_type.equals("security")) {
                id = R.drawable.security;
            } else if (object.poi_type.equals("sight")) {
                id = R.drawable.sightseeing;
            } else if (object.poi_type.equals("tourist")) {
                id = R.drawable.information;
            } else if (object.poi_type.equals("museum")) {
                id = R.drawable.museum;
            } else if (object.poi_type.equals("medical")) {
                id = R.drawable.medical;
            } else if (object.poi_type.equals("escalator")) {
                id = R.drawable.escalator;
            } else if (object.poi_type.equals("stairs")) {
                id = R.drawable.stairs;
            } else if (object.poi_type.equals("washroom")) {
                id = R.drawable.washroom;
            } else if (object.poi_type.equals("souvenir")) {
                id = R.drawable.store;
            }
            Drawable icon = DrawableCompat.wrap(ResourcesCompat.getDrawable(HKUApplication.sAPP.getResources(), id, null));
            Bitmap bitmapIcon = ((BitmapDrawable) icon).getBitmap();

            LatLng MELBOURNE = new LatLng(object.latitude, object.longitude);
            Marker mark=mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(bitmapIcon))
                    .position(MELBOURNE)
                    .anchor(0.5f,0.5f)
                    .zIndex(0));
            mark.setObject(object);
            POIMarker.add(mark);

        }
        int zoomLevel;
        zoomLevel = GeoUtils.getZoomCode((int)mMap.getCameraPosition().zoom);
        updatePOIPointWithZoom(zoomLevel);
    }
    public void updateFloorPlanWithLevel(String level) {
        cleanFloorPlan();
        addFloorPlanWithLevel(level);
    }
    public void setmLocation(){
        if(myLocationMarker==null&&useCheck==false){
            LatLng targetLocation=new LatLng(22.2834981258,114.134145211);
            DeadReckoningManager.getInstance().processDeadReckoningCoordinate(targetLocation, false);
        }
    }

    public void cleanFloorPlan() {
        if (mGroundOverlay != null) {
            mGroundOverlay.remove();
            mGroundOverlay = null;
            floorPlan = null;
        }
    }

    public void addFloorPlanWithLevel(String level) {
        IDFloorPlan plan = new IDFloorPlan(level);

        GroundOverlayOptions overlay = plan.groundOverlay();
        mGroundOverlay = mMap.addGroundOverlay(overlay);
        floorPlan = plan;
    }


    public void updateBuildingBoundaryWithLevel(String level) {
        cleanBuildingBoundary();
        addBuildingBoundaryWithLevel(level);
    }

    public void cleanBuildingBoundary() {
        for (Polygon polygon : buildingBoundaries) {
            polygon.remove();
        }
        buildingBoundaries.clear();
    }

    public void addBuildingBoundaryWithLevel(String level) {

        Resources resources = HKUApplication.sAPP.getResources();

        int fillColor = resources.getColor(R.color.colorFloor);
        int strokeColor = resources.getColor(R.color.colorFloorStroke);
        float strokeWidth = resources.getDimension(R.dimen.dp_1);

        if (routeCollection != null && mode == MapManagerModeNavigation) {
            if (level.equals(selectedPlate.levelCode)) {
                fillColor = resources.getColor(R.color.colorNa);
                strokeColor = resources.getColor(R.color.colorNaStroke);
            }
        }

        List<SPBuildingBoundary> result = DataIOManager.getInstance().boundaryForLevel(level);

        for (SPBuildingBoundary object : result) {

            List<LatLng> latLngs = object.boundaryPath();
            LatLng[] lngs = new LatLng[latLngs.size()];
            latLngs.toArray(lngs);

            PolygonOptions polygonOptions = new PolygonOptions()
                    .strokeColor(strokeColor)
                    .strokeWidth(strokeWidth)
                    .fillColor(fillColor)
                    //.geodesic(true)
                    .add(lngs);

            buildingBoundaries.add(mMap.addPolygon(polygonOptions));
        }
    }

    @Override
    public void didRangeBeacons(Collection<Beacon> beacons, Region region) {
        if (!beacons.isEmpty()) {
            DataIOManager.getInstance().mOften.onNext(new Often(0, beacons.size()));
        }
        RangingManager.getInstance().updateRegionWithUUID(region, beacons);
        RangingManager.getInstance().prepareNotification();
    }


    @Override
    public void didUpdateHeading(float heading) {
        updateMyLocationHeading(heading);
        DeadReckoningManager.getInstance().updateHeading(heading);

        for (MapIOManagerObserverableProtocol observer : observerArray) {
            observer.didUpdatedHeading(heading);
        }
    }

    @Override
    public void didUpdateLocations(Location location) {
        mLocation = location;
        //float radius = location.getAccuracy();
        //if (PrecisionLevelManager.getInstance().canUpdateLocation(location)) {
            for (MapIOManagerObserverableProtocol observer : observerArray) {
                observer.didUpdateLocations(location);
            }
        //}
    }

    @Override
    public void didRangedBeacons(List<Beacon> beacons) {
        for (MapIOManagerObserverableProtocol observer : observerArray) {
            observer.didRangedBeaconsMap(beacons);
        }
    }

    @Override
    public void didRangedNearestBeacon(Beacon beacon) {
        SPPlateModel plate = RangingManager.getInstance().plateForBeacon(beacon);
        if (plate == null) {
            return;
        }

        DataIOManager.getInstance().mSubject.onNext("最近plate:" + plate.toString() + "\n");
        nearestBeacon = beacon;
        updateNearestPlateModel(plate);
        updateLevel(plate);

        DeadReckoningManager.getInstance().approachPlate(plate);

        if (mode == MapManagerModePositioning) return;

        for (MapIOManagerObserverableProtocol delegate : observerArray) {
            delegate.didArrivedAtPlate(plate);
        }
    }

    @Override
    public void didCalculatedDeadReckoningModel(IDMatchingModel model, IDRouteModel route) {
        updateMyLocationModel(model);
        arrivalDestinationChecking();
    }

    @Override
    public void didChangedRouteFrom(IDRouteModel from, IDRouteModel to) {
    }

    @Override
    public void didExitConfidenceRangeFromPoint(IDPointModel point) {
    }

    public void addNotifyObserver(MapIOManagerObserverableProtocol observer) {
        synchronized (this) {
            if (observerArray.contains(observer)) {
                return;
            }
            observerArray.add(observer);
        }
    }

    public void removeNotifyObserver(MapIOManagerObserverableProtocol observer) {
        synchronized (this) {
            if (!observerArray.contains(observer)) {
                return;
            }
            observerArray.remove(observer);
        }
    }


    public interface MapIOManagerObserverableProtocol {
        default void managerDidInsideLift(MapIOManager manager) {
        }

        default void shouldChangeToLevel(String level, boolean preview) {
        }

        default void didUpdateDestinationPlate(SPPlateModel plate) {
        }

        default void didDisplayedShortestPath(IDRouteCollection collection, List<SPSelectedIndex> indexes) {
        }

        default void managerDidRemovedShortestPath(MapIOManager manager) {
        }

        default void didArrivedDestination(SPPlateModel plate) {
        }

        default void didArrivedAtPlate(SPPlateModel plate) {
        }

        default void didTapAtCoordinate(LatLng coordinate) {
        }

        default void didUpdatedHeading(float heading) {
        }

        default void didUpdateLocations(Location location) {
        }

        default void shouldChangeUserLocationButtonType(SPUserLocationButtonType type) {

        }

        default void didRangedBeaconsMap(List<Beacon> beacons) {
        }
    }
}
