public class Pair<K, V> {

    private  String element0;
    private  String element1;

    public static <K, V> Pair<K, V> createPair(String element0, String element1) {
        return new Pair<K, V>(element0, element1);
    }

    public Pair(String element0, String element1) {
        this.element0 = element0;
        this.element1 = element1;
    }

    public String getElement0() {
        return element0;
    }

    public String getElement1() {
        return element1;
    }

    public void setElements(String element0, String element1) {
        this.element0 = element0;
        this.element1 = element1;
    }

    //printing method for this class
    public String toString() {
        return "<" + this.element0 + "," + this.element1 + ">";
    }


    // You will need to override the methods .equals() and .hashCode() for your Pair() type.
    //This way I can check for Pair equals and I get get HashMap values
    //because I'm using Pair Objects as keys in the method HashMap
     

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Pair)) {
            return false;
        }

        final Pair pair = (Pair) o;

        if (element0 != pair.element0) {
            return false;
        }
        if (element1 != pair.element1) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        String result = element0 + element1;
        return result.hashCode();
    }


}