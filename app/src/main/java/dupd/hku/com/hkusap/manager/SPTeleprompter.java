package dupd.hku.com.hkusap.manager;

import com.amap.api.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dupd.hku.com.hkusap.R;
import dupd.hku.com.hkusap.model.IDPointModel;
import dupd.hku.com.hkusap.model.IDRouteModel;
import dupd.hku.com.hkusap.model.IEnum.SPWeightType;
import dupd.hku.com.hkusap.utils.GeoUtils;
import dupd.hku.com.hkusap.utils.Utils;

public class SPTeleprompter {

    private double WALKING_SPEED = 1.4;
    public List<IDRouteModel> routes;
    public List<SPTeleprompterGroup> groups;
    public List<SPTeleprompterGroup> liftGroups;

    public SPTeleprompter(List<IDRouteModel> routes) {
        this.routes = routes;
        separateRoutesIntoGroup();
    }

    public void separateRoutesIntoGroup() {
        SPTeleprompterGroup group = null;
        IDRouteModel previousRoute = null;
        int index=0;
        liftGroups = new ArrayList<>();
        groups = new ArrayList<>();
        for (IDRouteModel route : routes) {
            index++;
            if (previousRoute == null) {
                group = new SPTeleprompterGroup();
                previousRoute = route;
                group.appendRoute(route);
            } else {
                if (previousRoute.liftRoute()) {
                    if (route.liftRoute()) {
                        previousRoute = route;
                        group.appendRoute(route);
                    } else {
                        previousRoute = route;
                        if (group.routeCount() > 0) {
                            groups.add(group);
                            group = null;
                        }
                        group = new SPTeleprompterGroup();
                        group.appendRoute(route);
                    }
                } else {
                    if (route.liftRoute()) {
                        previousRoute = route;
                        if (group.routeCount() > 0) {
                            groups.add(group);
                            group = null;
                        }
                        group = new SPTeleprompterGroup();
                        group.appendRoute(route);
                    }else if (route.escalatorRoute()) {
                        previousRoute = route;
                        if (group.routeCount() > 0) {
                            groups.add(group);
                            group = null;
                        }
                        group = new SPTeleprompterGroup();
                        group.appendRoute(route);
                    }else {
                        double difference = headingDifferenceFromRoute(previousRoute, route);

                        previousRoute = route;
                        if (headingDifferenceAvailableForSameGroup(difference)) {
                            group.appendRoute(route);
                        } else {
                            if (group.routeCount() > 0) {
                                groups.add(group);
                                group = null;
                            }
                            group = new SPTeleprompterGroup();
                            group.appendRoute(route);
                        }
                    }
                }
            }
            if(index==groups.size()&&group.routeCount() > 0){
                groups.add(group);
                group = null;
            }
        }
        for(SPTeleprompterGroup group1:groups){
            if(group1.groupInLift())
                liftGroups.add(group1);
        }
    }

    public SPTeleprompterGroup nearestLiftGroup(LatLng coordinate) {
        if(liftGroups==null||liftGroups.size()==0)
            return null;
        int index=0;
        double distance=GeoUtils.distance(coordinate,liftGroups.get(0).firstRoute().start.coordinate);
        for(int i=1;i<liftGroups.size();i++){
            double distance1=GeoUtils.distance(coordinate,liftGroups.get(i).firstRoute().start.coordinate);
            if (distance>distance1) {
                index = i;
                distance=distance1;
            }
        }
        return liftGroups.get(index);
    }


    public List<SPTeleprompterGroup> routeGroups() {
        return new ArrayList<>(groups);
    }

    public IDRouteModel routeForPoint(IDPointModel point) {
        return DeadReckoningManager.getInstance().getNearestRouteFromPoint(point, routes);
    }

    public SPTeleprompterGroup groupForRoute(IDRouteModel route) {
        SPTeleprompterGroup result = null;
        if (groups == null) {
            return null;
        }
        int i=0;
        for (SPTeleprompterGroup group : groups) {
            if (group.containsRoute(route)) {
                result = group;
                break;
            }
        }
        return result;
    }

    public SPTeleprompterGroup upcommingGroupForRoute(IDRouteModel route) {
        SPTeleprompterGroup group = groupForRoute(route);
        if (group == null) return null;
        if (groups == null) {
            return null;
        }
        int index = groups.indexOf(group);
        if (index != groups.size() - 1) {
            return groups.get(index + 1);
        }
        return null;
    }


    public SPTeleprompterGroup nextGroup(SPTeleprompterGroup group) {
        if (groups == null) {
            return null;
        }
        int index = groups.indexOf(group);
        if (index != groups.size() - 1) {
            return groups.get(index + 1);
        }

        return null;
    }


    public double headingDifferenceFromRoute(IDRouteModel from, IDRouteModel to) {
        double fromHeading = DeadReckoningManager.getInstance().headingFrom(from.start.coordinate, from.end.coordinate);
        double toHeading = DeadReckoningManager.getInstance().headingFrom(to.start.coordinate, to.end.coordinate);
        double difference = fromHeading - toHeading;
        return difference;
    }


    public boolean headingDifferenceAvailableForSameGroup(double heading) {
        heading = Math.abs(heading);
        if (heading > 180) {
            heading = 360 - heading;
        }

        return heading <= 30;
    }


    public int directionImageAtIndex(int index) {
        if (index == 0) {
            return Utils.getResourcesDrawable("from_marker_mini");
        } else {
            if (index == groups.size() - 1) {
                //destination route
                return Utils.getResourcesDrawable("to_marker_mini");
            } else {
                SPTeleprompterGroup from = groups.get(index - 1);
                SPTeleprompterGroup to = groups.get(index);
                if (from.groupInLift()) {
                    return Utils.getResourcesDrawable(imageNameForHeadingDifference(0));
                } else {
                    if (to.groupInLift()) {
                        return R.drawable.circle;
                    } else {
                        double headingDiference = headingDifferenceFromRoute(from.lastRoute(), to.firstRoute());
                        return Utils.getResourcesDrawable(imageNameForHeadingDifference(headingDiference));
                    }
                }
            }
        }
    }


    public String directionDescriptionAtIndex(int index) {
        String wording;
        if (index == 0) {

            SPTeleprompterGroup first = groups.get(0);
            if (first.groupInLift()) {
                wording = "Take the Lift";
            } else {
                double heading = DeadReckoningManager.getInstance().headingFrom(first.firstRoute().start.coordinate, first.firstRoute().end.coordinate);
                wording = headingDescription(heading);
            }
        } else {
            SPTeleprompterGroup group = groups.get(index);
            SPTeleprompterGroup previous = groups.get(index - 1);

            if (previous.groupInLift()) {
                if (group.groupInLift()) {
                    wording = "Take the Lift";
                } else {
                    wording = headingDifferenceDecription(0);
                }
            } else {
                if (group.groupInLift()) {
                    if (index == groups.size() - 1) {
                        //destination
                        wording = String.format("Take the Lift to destination: %s", MapIOManager.getInstance().destinationPlate().name);
                    } else {
                        wording = "Take the Lift";
                    }
                } else {
                    double headingDifference = headingDifferenceFromRoute(previous.lastRoute(), group.firstRoute());
                    wording = headingDifferenceDecription(headingDifference);

                    if (index == groups.size() - 1) {
                        //destination
                        wording = String.format("%s to destination: %s", wording, MapIOManager.getInstance().destinationPlate().name);
                    }
                }
            }
        }
        return wording;
    }


    public String subtitleDescriptionAtIndex(int index) {
        String wording;
        if (index == 0) {

            SPTeleprompterGroup first = groups.get(0);
            if (first.groupInLift()) {
                wording = String.format("%s - %s", DataIOManager.getInstance().abbreviationForLevelCode(first.firstRoute().start.levelCode), DataIOManager.getInstance().abbreviationForLevelCode(first.lastRoute().end.levelCode));
            } else {
                double distance = first.groupLength();
                wording = distanceDescription(distance, false);
                String appending = typeAppendingWording(first.groupType());
                wording = wording + appending;
            }
        } else {

            SPTeleprompterGroup group = groups.get(index);
            if (group.groupInLift()) {
                wording = String.format("%s - %s", DataIOManager.getInstance().abbreviationForLevelCode(group.firstRoute().start.levelCode), DataIOManager.getInstance().abbreviationForLevelCode(group.lastRoute().end.levelCode));
            } else {
                if (index == groups.size() - 1) {
                    //destination
                    double distance = group.groupLength();
                    wording = distanceDescription(distance, false);
                } else {
                    double distance = group.groupLength();
                    wording = distanceDescription(distance, false);

                    String appending = typeAppendingWording(group.groupType());
                    wording = wording + appending;
                }
            }
        }

        return wording;
    }

    public String typeAppendingWording(SPWeightType type) {
        if (type == null) {
            return "";
        }
        switch (type) {
            case SPWeightTypeDoor:
                return " to the room";
            case SPWeightTypeLift:
                return " to the lift";
            case SPWeightTypeEntrance:
                return " to the building entrance";
            case SPWeightTypeEscalator:
                return "to the escalator";
            case SPWeightTypeOthers:
            default:
                return "";
        }
    }

    public String headingDescription(double heading) {
        String wording;
        if (heading < 22.5) {
            wording = "Head North";
        } else if (heading < 67.5) {
            wording = "Head Northeast";
        } else if (heading < 117.5) {
            wording = "Head East";
        } else if (heading < 162.5) {
            wording = "Head Southeast";
        } else if (heading < 207.5) {
            wording = "Head South";
        } else if (heading < 252.5) {
            wording = "Head Southwest";
        } else if (heading < 297.5) {
            wording = "Head South";
        } else if (heading < 342.5) {
            wording = "Head Northwest";
        } else {
            wording = "Head North";
        }
        return wording;
    }

    public String headingDifferenceDecription(double difference) {
        String description;
        if (difference > 0) {
            if (difference <= 30) {
                description = "Go Straight forward";
            } else if (difference <= 60) {
                description = "Turn Left Slightly";
            } else if (difference <= 150) {
                description = "Turn Left";
            } else if (difference <= 210) {
                description = "Turn Backward";
            } else if (difference <= 300) {
                description = "Turn Right";
            } else if (difference <= 330) {
                description = "Turn Slightly Right";
            } else {
                description = "Go Straight Forward";
            }
        } else {
            if (difference <= -330) {
                description = "Go Straight Forward";
            } else if (difference <= -300) {
                description = "Turn Left Slightly";
            } else if (difference <= -210) {
                description = "Turn Left";
            } else if (difference <= -150) {
                description = "Turn Backward";
            } else if (difference <= -60) {
                description = "Turn Right";
            } else if (difference <= -30) {
                description = "Turn Slightly Right";
            } else {
                description = "Go Slightly Forward";
            }
        }

        return description;
    }


    public String imageNameForHeadingDifference(double difference) {
        String imageName;

        if (difference > 0) {
            if (difference <= 30) {
                imageName = "straight";
            } else if (difference <= 60) {
                imageName = "left_s";
            } else if (difference <= 150) {
                imageName = "left";
            } else if (difference <= 210) {
                imageName = "back_route";
            } else if (difference <= 300) {
                imageName = "right";
            } else if (difference <= 330) {
                imageName = "right_r";
            } else {
                imageName = "straight";
            }
        } else {
            if (difference <= -330) {
                imageName = "straight";
            } else if (difference <= -300) {
                imageName = "left_s";
            } else if (difference <= -210) {
                imageName = "left";
            } else if (difference <= -150) {
                imageName = "back_route";
            } else if (difference <= -60) {
                imageName = "right";
            } else if (difference <= -30) {
                imageName = "right_s";
            } else {
                imageName = "straight";
            }
        }

        return imageName;
    }

    public String distanceDescription(double distance, boolean with) {
        int meters = (int) distance;
        if (meters > 1000) {
            return String.format(Locale.CHINA, "%d %skm", (long) (meters / 1000.0), (with ? " " : ""));
        }

        return String.format(Locale.CHINA, "%d %sm", (long) meters, (with ? " " : ""));
    }

    public String distanceDescription(double distance) {
        return distanceDescription(distance, true);
    }


    public String timeIntervalDescription(double interval) {
        if (interval < 60) {
            return String.format(Locale.CHINA, "%d seconds", (int) interval);
        } else if (interval < 3600) {
            return String.format(Locale.CHINA, "%d minutes", (int) Math.ceil(interval / 60.0));
        } else {
            double hours = Math.floor(interval / 3600.0);
            double minutes = Math.floor((interval - hours * 3600) / 60.0);
            return String.format(Locale.CHINA, "%d hour %d minutes", (long) hours, (long) minutes);
        }
    }


    public String timeIntervalForDistance(double distance) {
        long interval = Math.round(distance / WALKING_SPEED);
        return timeIntervalDescription(interval);
    }

//    public double calculateDistanceInMeters() {
//        double distance = 0.0F;
//        for (IDRouteModel route : routes) {
//            distance += GeoUtils.distance(route.start.coordinate, route.end.coordinate);
//        }
//        return distance;
//    }
//
//
//    public int directionImageNameAtIndex(int index) {
//        if (index == 0) {
//            return 0;
//        } else {
//            if (index == routes.size() - 1) {
//                //destination route
//                return 0;
//            } else {
//                IDRouteModel fromRoute = routes.get(index);
//                IDRouteModel toRoute = routes.get(index + 1);
//                double fromHeading = DeadReckoningManager.getInstance().headingFrom(fromRoute.start.coordinate, fromRoute.end.coordinate);
//                double toHeading = DeadReckoningManager.getInstance().headingFrom(toRoute.start.coordinate, toRoute.end.coordinate);
//
//                String imageName = imageNameForHeadingDifference(fromHeading - toHeading);
//                return Utils.getResourcesDrawable(imageName);
//            }
//        }
//    }
//
//    public String subtitleDescriptionAtCoordinate(LatLng coordinate, IDRouteModel route) {
//        double distance = GeoUtils.distance(coordinate, route.end.coordinate);
//        String wording = distanceDescription(distance);
//        String appending = typeAppendingWording(route.end.type);
//        return wording + appending;
//    }
}
