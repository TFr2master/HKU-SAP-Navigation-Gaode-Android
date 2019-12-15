package dupd.hku.com.hkusap.manager;

public enum MapManagerMode {
    MapManagerModePositioning(0),
    MapManagerModeRouteFinding(1),
    MapManagerModeNavigation(2);

    private int value;

    MapManagerMode(int value) {
        this.value = value;
    }
}
