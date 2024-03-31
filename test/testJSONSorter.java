import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class testJSONSorter {

    public static void moveItemInJSONArray(JSONArray jsonArray, int index, boolean moveUp) {
        if (moveUp) {
            moveItemUp(jsonArray, index);
        } else{
            moveItemDown(jsonArray, index);
        }
    }

    private static void moveItemUp(JSONArray jsonArray, int index) {
        if (index > 0 && index < jsonArray.size()) {
            Object currentItem = jsonArray.remove(index);
            jsonArray.add(index - 1, currentItem);
        }
    }

    private static void moveItemDown(JSONArray jsonArray, int index) {
        if (index >= 0 && index < jsonArray.size() - 1) {
            Object currentItem = jsonArray.remove(index);
            jsonArray.add(index + 1, currentItem);
        }
    }

    public static void main(String[] args) {
        // Example usage
        JSONArray jsonArray = new JSONArray();
        JSONObject obj1 = new JSONObject();
        obj1.put("name", "John");
        obj1.put("age", 30);
        jsonArray.add(obj1);

        JSONObject obj2 = new JSONObject();
        obj2.put("name", "Alice");
        obj2.put("age", 25);
        jsonArray.add(obj2);

        JSONObject obj3 = new JSONObject();
        obj3.put("name", "Bob");
        obj3.put("age", 35);
        jsonArray.add(obj3);
        
        System.out.println(jsonArray.toJSONString());
        
        // Move item at index 1 up
        moveItemInJSONArray(jsonArray, 1, true);

        System.out.println(jsonArray.toJSONString());
        
        // Move item at index 2 down
        moveItemInJSONArray(jsonArray, 1, false);

        System.out.println(jsonArray.toJSONString());
    }
}