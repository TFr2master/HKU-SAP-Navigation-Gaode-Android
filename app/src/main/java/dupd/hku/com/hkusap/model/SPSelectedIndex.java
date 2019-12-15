package dupd.hku.com.hkusap.model;

import dupd.hku.com.hkusap.model.IEnum.SPSelectedIndexType;

public class SPSelectedIndex {

    public SPSelectedIndexType type;
    public int indicatorIndex;

    public SPSelectedIndex(int indicatorIndex, SPSelectedIndexType type) {
        this.indicatorIndex = indicatorIndex;
        this.type = type;
    }
}
