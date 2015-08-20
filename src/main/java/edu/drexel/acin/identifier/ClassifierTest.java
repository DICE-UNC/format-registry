/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.drexel.acin.identifier;

import java.io.IOException;

/**
 * @author omodolu95
 */
public class ClassifierTest {
    public static void main(String[] args) throws IOException {
        FiletypeExample ex1 = new FiletypeExample("/home/omodolu95/formatregistry-data/EngineeringFileFormat/httpwww.web3d.orgx3dspecificationsx3d/Mars.x3d");
        FiletypeExample ex2 = new FiletypeExample("/home/omodolu95/formatregistry-data/EngineeringFileFormat/httpwww.web3d.orgx3dspecificationsx3d/Mexico.x3d");
        FiletypeExample ex3 = new FiletypeExample("/home/omodolu95/formatregistry-data/EngineeringFileFormat/httpsketchup.google.com/Unbenannt.skp");


        System.out.println("Size of 1 = " + ex1.getCompressedSize());
        System.out.println("Size of 2 = " + ex2.getCompressedSize());
        System.out.println("Size of 3 = " + ex3.getCompressedSize());

        System.out.println("Size of 1,2 = " + ex1.computeCompressedCombinedSize(ex2));
        System.out.println("Size of 1,3 = " + ex1.computeCompressedCombinedSize(ex3));
        System.out.println("Size of 2,3 = " + ex2.computeCompressedCombinedSize(ex3));
        System.out.println("Size of 1,1 = " + ex1.computeCompressedCombinedSize(ex1));
        System.out.println("Size of 2,2 = " + ex2.computeCompressedCombinedSize(ex2));
        System.out.println("Size of 3,3 = " + ex3.computeCompressedCombinedSize(ex3));

    }

}
