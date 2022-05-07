
public class Test{
    public static void main(String[] args){
      Vehicle car = new Vehicle("車");
      Vehicle bike = new Vehicle("バイク");
      Vehicle airplane = new Vehicle("飛行機");
       }
    }
    
    class Vehicle{
    public static int num = 0;
      public Vehicle(String data){
      num++; // 何回インスタンス化されたかをカウントする
      System.out.println( data + ":" + String.valueOf(num));
              }
    }