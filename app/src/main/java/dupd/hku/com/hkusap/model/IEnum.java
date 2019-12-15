package dupd.hku.com.hkusap.model;

import dupd.hku.com.hkusap.R;

public interface IEnum {

    enum IDPointPosition {
        IDPointPositionMyLocation(0),
        IDPointPositionDestination(1),
        IDPointPositionOthers(2);

        public int position;

        IDPointPosition(int position) {
            this.position = position;
        }
    }

    enum SPWeightType {
        SPWeightTypeEntrance(1),
        SPWeightTypeLift(2),
        SPWeightTypeDoor(3),
        SPWeightTypeEscalator(4),
        SPWeightTypeOthers(9);

        public int weightType;

        SPWeightType(int weightType) {
            this.weightType = weightType;
        }

        public static SPWeightType MIN(SPWeightType a, SPWeightType b) {
            int min = Math.min(a.weightType, b.weightType);
            return init(min);
        }

        public static SPWeightType init(int weightType) {
            switch (weightType) {
                case 1:
                    return SPWeightTypeEntrance;
                case 2:
                    return SPWeightTypeLift;
                case 3:
                    return SPWeightTypeDoor;
                case 4:
                    return SPWeightTypeEscalator;
                case 9:
                default:
                    return SPWeightTypeOthers;
            }
        }
    }

    enum IDPointUsage {
        SPPointUsageBuildingBoundary(0),
        SPPointUsageRoomBoundary(1),
        SPPointUsageRoute(2),
        SPPointUsageBase(3);

        public int usage;

        IDPointUsage(int usage) {
            this.usage = usage;
        }
    }


    enum SPSelectedIndexType {
        SPSelectedIndexTypeNone,
        SPSelectedIndexTypeFrom,
        SPSelectedIndexTypeTransition,
        SPSelectedIndexTypeTo;
    }

    enum SPUserLocationButtonType {
        SPUserLocationButtonNormal,
        SPUserLocationButtonHightlighted,
        SPUserLocationButtonTracking;
    }


    default int imageForType(SPWeightType type) {
        switch (type) {
            case SPWeightTypeDoor:
                return R.drawable.door_m;
            case SPWeightTypeLift:
                return R.drawable.lift_m;
            case SPWeightTypeEntrance:
                return R.drawable.entrance_m;
            case SPWeightTypeEscalator:
                return R.drawable.escalater_m;
            case SPWeightTypeOthers:
            default:
                return 0;
        }
    }

}
