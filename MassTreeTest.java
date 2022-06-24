import java.util.Random;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MassTreeTest {
    public static <T extends Comparable<T>> void assertEqual(T obj, T expected) {
        if (!obj.equals(expected)) {
            StringBuilder builder = new StringBuilder();
            builder.append("Assertion failed: ");
            builder.append(obj);
            builder.append(" is not equals to ");
            builder.append(expected);
            throw new AssertionError(builder.toString());
        }
    }

    public static <T> void assertNotNull(T obj) {
        if (obj == null) {
            StringBuilder builder = new StringBuilder();
            builder.append("Assertion failed: ");
            builder.append(obj);
            builder.append(" is null");
            throw new AssertionError(builder.toString());
        }
    }


    public static void main(String[] args){
        MassTree tree = new MassTree();
        Random rand = new Random(0); 
        int[] intArray0 = rand.ints(100, 10500, 11500).toArray();
        int[] intArray1 = rand.ints(70, 0, 70).toArray();
        HashMap<String, String> m = new HashMap<String, String>();
        for(int i = 0; i < intArray0.length; i++){
            m.put(String.valueOf(intArray0[i]), String.valueOf(intArray0[i]));
            tree.put(String.valueOf(intArray0[i]), String.valueOf(intArray0[i]));
            System.out.println(intArray0[i] + " inserted");
        }
        tree.validate();
        tree.rootTree.makeDotFile();
        System.out.println(m.size() + " " + tree.getrange("",10000).size());
        // for(int i = 0; i < intArray0.length; i++){ 
        //     System.out.println("key "+ tree.get(String.valueOf(intArray0[i])));
        // }
        
        // tree.put("1"," ");
        // {
        //     MassTree.MassTreeNode.BorderNode root = (MassTree.MassTreeNode.BorderNode)tree.rootTree.root;
        //     assertNotNull(root);
        //     assertEqual(root.nkeys, 1);
        //     assertEqual(root.keys[0], "1");
        // }
        // tree.put("2"," ");
        // {
        //     MassTree.MassTreeNode.BorderNode root = (MassTree.MassTreeNode.BorderNode)tree.rootTree.root;
        //     assertNotNull(root);
        //     assertEqual(root.nkeys, 2);
        //     assertEqual(root.keys[0], "1");
        //     assertEqual(root.keys[1], "2");
        // }
        // tree.put("3"," ");
        // tree.put("4"," ");
        // tree.put("5"," ");
        // tree.put("9"," ");
        // tree.put("10"," ");
        // tree.put("11"," ");
        // tree.put("12"," ");
        // tree.put("6"," ");
        // tree.put("7"," ");
        // tree.put("8"," ");
        // tree.rootTree.makeDotFile();
        // {
        //     MassTree.MassTreeNode.InteriorNode root = (MassTree.MassTreeNode.InteriorNode)tree.rootTree.root;
        //     assertNotNull(root);
        //     assertEqual(tree.rootTree.root.nkeys, 1);
        //     assertEqual(tree.rootTree.root.keys[0], "4");
        //     MassTree.MassTreeNode.BorderNode leftChild = (MassTree.MassTreeNode.BorderNode)root.child[0];
        //     assertNotNull(leftChild);
        //     assertEqual(leftChild.nkeys, 6);
        //     assertEqual(leftChild.keys[0], "1");
        //     assertEqual(leftChild.keys[5], "3");
        //     MassTree.MassTreeNode.BorderNode rightChild = (MassTree.MassTreeNode.BorderNode)root.child[1];
        //     assertNotNull(rightChild);
        //     assertEqual(rightChild.nkeys, 6);
        //     assertEqual(rightChild.keys[0], "4");
        //     assertEqual(rightChild.keys[5], "9");
        // }

        // tree.put("10000","10000");
        // {
        //     MassTree.MassTreeNode.BorderNode root = (MassTree.MassTreeNode.BorderNode)tree.rootTree.root;
        //     assertNotNull(root);
        //     assertEqual(root.nkeys, 1);
        //     assertEqual(root.keys[0], "10");
        // }
        // tree.put("11000","11000");
        // {
        //     MassTree.MassTreeNode.BorderNode root = (MassTree.MassTreeNode.BorderNode)tree.rootTree.root;
        //     assertNotNull(root);
        //     assertEqual(root.nkeys, 2);
        //     assertEqual(root.keys[0], "10");
        //     assertEqual(root.keys[1], "11");
        // }
        // tree.put("10003","10003");
        // tree.put("10004","10004");
        // tree.put("10010","10010");
        // tree.put("10011","10011");
        // tree.put("10012","10012");
        // tree.put("11004","11004");
        // tree.put("11003","11003");
        // tree.put("11005","11005");
        // tree.put("11002","11002");
        // tree.put("10008","10008");
        // tree.rootTree.makeDotFile();
        // {
        //     MassTree.MassTreeNode.BorderNode root = (MassTree.MassTreeNode.BorderNode)tree.rootTree.root;
        //     assertNotNull(root);
        //     assertEqual(tree.rootTree.root.nkeys, 2);
        //     assertEqual(tree.rootTree.root.keys[0], "10");
        //     MassTree.MassTreeNode.Layer layer10 = (MassTree.MassTreeNode.Layer)root.data[0];
        //     assertNotNull(layer10);
        //     MassTree.MassTreeNode.BorderNode layer10Root = (MassTree.MassTreeNode.BorderNode)layer10.nextLayer.root;
        //     assertNotNull(layer10Root);
        //     assertEqual(layer10Root.nkeys, 2);
        //     assertEqual(layer10Root.keys[0], "00");
        //     assertEqual(layer10Root.keys[1], "01");
        //     MassTree.MassTreeNode.Layer layer11 = (MassTree.MassTreeNode.Layer)root.data[1];
        //     MassTree.MassTreeNode.BorderNode layer11Root = (MassTree.MassTreeNode.BorderNode)layer11.nextLayer.root;
        //     assertNotNull(layer11);
        //     assertEqual(layer11Root.nkeys, 1);
        //     assertEqual(layer11Root.keys[0], "00");
        // }
        // assertEqual(tree.get("10000"), "10000");
        // assertEqual(tree.get("11000"), "11000");
        // assertEqual(tree.get("10003"), "10003");
        // assertEqual(tree.get("10004"), "10004");
        // assertEqual(tree.get("10010"), "10010");
        // assertEqual(tree.get("10011"), "10011");
        // assertEqual(tree.get("10012"), "10012");
        // assertEqual(tree.get("11004"), "11004");
        // assertEqual(tree.get("11003"), "11003");
        // assertEqual(tree.get("11005"), "11005");
        // assertEqual(tree.get("11002"), "11002");
        // assertEqual(tree.get("10008"), "10008");



        
        // List<String> ls = tree.getrange("10900",30);
        // for(int i = 0; i < ls.size(); i++){
        //     System.out.println("getrange from key \"10900\" #" + i + ": " + ls.get(i));
        // }
        // for(int i = 0; i < intArray0.length/2; i++){ 
        //     tree.delete(String.valueOf(intArray0[i]));
        //     System.out.println(intArray0[i] + " deleted");
        // }
        // for(int i = 0; i < intArray0.length; i++){ 
        //     System.out.println("key " + tree.get(String.valueOf(intArray0[i])));
        // }
        // for(int i = 0; i < intArray0.length; i++){
        //     System.out.println("key:" + String.valueOf(intArray0[i]) + " value:" + tree.get(String.valueOf(intArray0[i])));
        // }
        return;
    }
}