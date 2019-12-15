package dupd.hku.com.hkusap.model;

public interface GLKVector {

    class GLKVector3 {
        public double x;
        public double y;
        public double z;

        public GLKVector3(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    class GLKVector2 {

        public double x;
        public double y;

        public GLKVector2(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public GLKVector2(GLKVector2 vectorLeft, GLKVector2 vectorRight) {
            this.x = vectorLeft.x - vectorRight.x;
            this.y = vectorLeft.y - vectorRight.y;
        }


        public double length() {
            return Math.sqrt(x * x + y * y);
        }

    }
}
