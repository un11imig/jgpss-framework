/**
 * Software end-user license agreement.
 *
 * The LICENSE.TXT containing the license is located in the JGPSS project.
 * License.txt can be downloaded here:
 * href="http://www-eio.upc.es/~Pau/index.php?q=node/28
 *
 * NOTICE TO THE USER: BY COPYING, INSTALLING OR USING THIS SOFTWARE OR PART OF
 * THIS SOFTWARE, YOU AGREE TO THE   TERMS AND CONDITIONS OF THE LICENSE AGREEMENT
 * AS IF IT WERE A WRITTEN AGREEMENT NEGOTIATED AND SIGNED BY YOU. THE LICENSE
 * AGREEMENT IS ENFORCEABLE AGAINST YOU AND ANY OTHER LEGAL PERSON ACTING ON YOUR
 * BEHALF.
 * IF, AFTER READING THE TERMS AND CONDITIONS HEREIN, YOU DO NOT AGREE TO THEM,
 * YOU MAY NOT INSTALL THIS SOFTWARE ON YOUR COMPUTER.
 * UPC IS THE OWNER OF ALL THE INTELLECTUAL PROPERTY OF THE SOFTWARE AND ONLY
 * AUTHORIZES YOU TO USE THE SOFTWARE IN ACCORDANCE WITH THE TERMS SET OUT IN
 * THE LICENSE AGREEMENT.
 */

package jgpss;

import java.io.Serializable;
import java.util.*;

/**
 * A class representing the model elements.
 * @author Pau Fonseca i Casas
 * @version 1
 * @see     <a href="http://www-eio.upc.es/~Pau/index.php?q=node/28">Project website</a>
 * @serialData
 */
public class Model implements Serializable {
    
    private String nomModel;
    private String DescripModel;
    //private int id;
    //array of processes. Contains process objects
    private ArrayList proces;
    //array containing the storages.
    private ArrayList storages;
    //Future and Current Event List.
    private PriorityQueue CEC;
    private PriorityQueue FEC;
    private HashMap<String, PriorityQueue<Xact>> BEC;
    private HashMap<String, Integer> facilities;
    /**
     * The transaction counter.
     */
    public int TC;
    /**
     * Identifier for the XACT created until the moment.
     */
    public int idxact;
    /**
     * Absolute simulation clock.
     */
    public float absoluteClock;
    /**
     * Relative simulation clock.
     */
    public float relativeClock;
    /**
     * The GNA of the model.
     */
    public GNA MyRandom=new GNA();

    /**
     * Creates a new instance of Model.
     */
    Model() {
        //inicialitzem a una array buida ja que no tenim encara processos
        this.setProces(new ArrayList());
        this.setStorages(new ArrayList());
        
        CEC = new PriorityQueue<Xact>(1000, this.getPriorityComparator());
        FEC = new PriorityQueue<Xact>(1000, this.getTimeComparator());
        BEC = new HashMap<String, PriorityQueue<Xact>>();
        facilities = new HashMap<String, Integer>();
    }

    public Comparator<Xact> getPriorityComparator() {
        return new Comparator<Xact>() {
            public int compare(Xact o1, Xact o2) {
                if (o1.getPriority() > o2.getPriority()) return -1;
                else if (o1.getPriority() == o2.getPriority()) return 0;
                else return 1;
            }
        };
    }
    
    public Comparator<Xact> getTimeComparator() {
        return new Comparator<Xact>() {
            public int compare(Xact o1, Xact o2) {
                if (o1.getMoveTime() < o2.getMoveTime()) return -1;
                else if (o1.getMoveTime() == o2.getMoveTime()) return 0;
                else return 1;
            }
        };
    }
    
    /**
     * To obtaint the CEC.
     * @return the CEC.
     */
    public PriorityQueue getCEC() {
        return CEC;
    }

    /**
     * To obtain the FEC.
     * @return
     */
    public PriorityQueue getFEC() {
        return FEC;
    }
    
    /**
     * To obtain the BEC.
     * @return
     */
    public HashMap<String, PriorityQueue<Xact>> getBEC() {
        return BEC;
    }
    
    /**
     * To obtain the resources set.
     * @return
     */
    public HashMap<String,Integer> getFacilities() {
        return facilities;
    }

    /**
     * To obtain the processes (now an arraylist).
     * @return
     */
    public ArrayList getProces() {
        return proces;
    }

    /**
     * To set the proceses (now an arraylist).
     * @param proces
     */
    public void setProces(ArrayList proces) {
        this.proces = proces;
    }

    /**
     * To obtain the name of the model.
     * @return the name.
     */
    public String getNomModel() {
        return nomModel;
    }

    /**
     * To set the name of the model.
     * @param nomModel the new name.
     */
    public void setNomModel(String nomModel) {
        this.nomModel = nomModel;
    }

    /**
     * To obtain the description of the model.
     * @return the description.
     */
    public String getDescripModel() {
        return DescripModel;
    }

    /**
     * To set the description of the model.
     * @param DescripModel the description.
     */
    public void setDescripModel(String DescripModel) {
        this.DescripModel = DescripModel;
    }

    /**
     * To obtain the array list containing the STORAGES.
     * @return the arraylist.
     */
    public ArrayList getStorages() {
        return storages;
    }

    /**
     * To set the array list containing the STORAGES.
     * @param storages the new arraylist.
     */
    public void setStorages(ArrayList storages) {
        this.storages = storages;
    }

    /**
     * Error manegament
     */
    public void registerError(String message) {
        //Do something with the message
    }

    /**
     * To initialize the GENERATE block.
     * This method can be used as a template for other initialization procedures.
     */
    void InitializeGenerateBocs(){
        for (int j = 0; j < proces.size(); j++) {
            Proces p = (Proces) proces.get(j);

            for (int k = 0; k < p.getBlocs().size(); k++) {
                Bloc b = ((Bloc) (p.getBlocs().get(k)));
                if (b.getId()==Constants.idGenerate) {
                    ((Generate)b).execute(null);
                }
            }
        }
    }

    /**
     * To execute the simulation model.
     * @param b if true we execute the simulation step by step.
     */
    void execute(boolean b) {
        relativeClock=0;
        absoluteClock=0;
        InitializeGenerateBocs();
        if(!b) executeAll();
        else executeStep();
    }

    /**
     * To execute the simulation model.
     */
    void executeAll() {
        Xact xact;
        //Simulation engine loop.
        while (TC > 0) {
            // SCAN PHASE
            xact = (Xact) CEC.poll();
            while (xact != null) {
                Bloc b = xact.getBloc();
                do {
                    b.execute(xact);
                    b = b.nextBloc(xact);
                } while(b != null);
                xact = (Xact) CEC.poll();
            }
            
            // CLOCK UPDATE PHASE
            xact = (Xact) FEC.poll();
            if (xact != null) {
                relativeClock = xact.getMoveTime();
                do {
                    CEC.add(xact);
                    xact = (Xact) FEC.poll();
                } while (xact != null && xact.getMoveTime() == relativeClock);
            }
        }
        // SIM REPORT ???
    }

    /**
     * To execute a single step of the simulation model.
     * Executes untin a new CLOCK UPDATE PHASE.
     */
    void executeStep() {
        Xact xact;
        //Motor central de simulació.
        if (TC > 0) {
            //TODO 1: First XACT.
            //TODO 2: Move the XACT as far as we can.
            //TODO 3: Look for other NOW XACT.
            //TODO 4: CLOCK UPDATE PHASE
            //TODO 5: Move the Xacts of the FEC to the CEC.
            //TODO 6: Goto TODO 1.
        }
    }
}
