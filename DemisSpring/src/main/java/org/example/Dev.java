package org.example;

public class Dev {

    private Computer com;

    // private int age = 8;

    public Dev(){
        System.out.println("Dev Constructor");
    }

//    public Dev(int age){
//        this.age = age;
//        System.out.println("Dev 1 Constructor");
//    }
//    public Dev(Laptop laptop){
//        this.laptop = laptop;
//        System.out.println("Dev 1 Constructor");
//    }

//    public int getAge() {
//        return age;
//    }
//
//    public void setAge(int age) {
//        this.age = age;
//    }
    public Computer getCom() {
        return com;
    }

    public void setCom(Computer com) {
        this.com = com;
    }
//    public Laptop getLaptop() {
//        return laptop;
//    }
//
//    public void setLaptop(Laptop laptop) {
//        this.laptop = laptop;
//    }

    public void build(){

        System.out.println("Working on Awesome Project");
        // laptop.compile();
        com.compile();
    }
}
