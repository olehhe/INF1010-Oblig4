/*
 * Oppgaver:
 * -    Lag sortPick(array, low, n) slik at man kan velge utifra hvor mange
 *      elementer som skal sorteres
 * -    Legg til insert hvis 4 <= n <= 10
 * -    Legg til if-tester hvis 2 <= n <= 4
 *
 */

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.PrintStream;
import java.util.*;

public class Sort {
    public static void main(String[] args) {

        if(args.length > 0) {
            String filenameIn = args[1];
            String filenameOut = args[2];
            int threadCnt = Integer.parseInt(args[0]);
            
            new ioHandler().readFile(filenameIn, filenameOut, threadCnt);
        }
    }
}

class SortSlave implements Runnable {
    String[] items;
    Buffer buffer;
    
    public SortSlave(String[] arr, Buffer buf) {
        items = arr;
        buffer = buf;
    }

    public SortSlave() {
    }

    public void run() {
        sortPicker(items, 0, items.length);
    }

    

    protected void sortPicker(String[] items, int low, int n) {
        String[] itemz = items;
        
        if(n > 10) {
            quicksort(itemz, low, itemz.length-1);
            buffer.add(itemz); 
        } else if(n <= 10) {
            insertionsort(itemz, n);
            buffer.add(itemz);
        } else {
            throw new NullPointerException();
        }
    }

    private void quicksort(String[] items, int low, int n) {
        int lo = low;
        int hi = n;
        String[] itemz = items;

        String mid = itemz[(low + hi) / 2];
        String a;

        do {
            while(itemz[lo].compareTo(mid) < 0) {
                lo++;
            }

            while(itemz[hi].compareTo(mid) > 0) {
                hi--;
            }

            if(lo <= hi) {
                a = itemz[lo];
                itemz[lo] = itemz[hi];
                itemz[hi] = a;
                lo++;
                hi--;
            }
        } while(lo <= hi);

        if(low <  hi)
            quicksort(itemz, low, hi);

        if(lo < n)
            quicksort(itemz, lo, n);
        
    }

    protected void insertionsort(String[] items, int n) {
        int in, out;
        String[] itemz = items;

        for(out = 1; out < n; out++) {
            String t = itemz[out];
            in = out;
            while(in > 0 && itemz[in-1].compareTo(t) >= 0) {
                itemz[in] = itemz[in-1];
                --in;
            }

            itemz[in] = t;
        }
    }

    public String[] merger(String[] arr1, String[] arr2) {
        String[] result = new String[arr1.length + arr2.length];

        int j1 = 0; //arr1 index
        int j2 = 0; //arr2 index

        for(int i = 0; i < result.length; i++) {
            if(j1 < arr1.length) {
                if(j2 < arr2.length) {
                    if(arr1[j1].compareTo(arr2[j2]) < 0) {
                        result[i] = arr1[j1++];
                    } else {
                        result[i] = arr2[j2++];
                    }
                } else {
                    result[i] = arr1[j1++];
                }
            } else {
                result[i] = arr2[j2++];
            }
        }
        return result;
    }
}

class ioHandler {
    public int wordsCnt, threadCnt, splitCnt;
    private static String[] readArray;
    private int arrPos = 0;
    private BufferedReader br;
    private BufferedWriter bw;
    private String current = "";
    private static String inFile, outFile = "";
    
    public void readFile(String filenameIn, String filenameOut, int threadCnt) {   
        inFile = filenameIn;
        outFile = filenameOut;
        int index = 0;
        Buffer buffer;

        try {
            br = new BufferedReader(new FileReader(inFile));
            wordsCnt = Integer.parseInt(br.readLine());
            readArray = new String[wordsCnt];
            buffer = new Buffer(threadCnt, wordsCnt);
            splitCnt = wordsCnt / threadCnt;
            
            if(splitCnt < 1)
                System.exit(0);

            while((current = br.readLine()) != null) {
                readArray[arrPos++] = current;

                if(arrPos % splitCnt == 0) {
                    if(readArray.length-arrPos < splitCnt) {
                        String[] temp = new String[(readArray.length - arrPos) + splitCnt];
                        for(int i = 0; i < temp.length; i++) {
                            temp[i] = readArray[index++];
                        }
                        new Thread(new SortSlave(temp, buffer)).start();
                    } else if(arrPos % splitCnt == 0) {
                        String[] temp = new String[splitCnt];

                        for(int i = 0; i < splitCnt; i++) {
                            temp[i] = readArray[index++];
                        }
                        new Thread(new SortSlave(temp, buffer)).start();
                    }
                }
                /*
                if(arrPos == readArray.length && wordsCnt % threadCnt != 0) {
                    String[] temp = new String[wordsCnt % threadCnt];
                    
                    for(int i = 0; i < (wordsCnt % threadCnt); i++) {
                        temp[i] = readArray[index++];
                    }
                    
                    new Thread(new SortSlave(temp, buffer)).start();
                } else if(arrPos % splitCnt == 0) {
                    String[] temp = new String[splitCnt];
                    
                    for(int i = 0; i < splitCnt; i++) {
                        temp[i] = readArray[index++];
                    }
                    
                    new Thread(new SortSlave(temp, buffer)).start();
                }*/
            } 
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(br != null)
                br.close();
            } catch(IOException e) {
                System.err.println("IOException!");
                System.exit(0);
            }
        }
    }

    public void fillAndWrite(String[] sortedArray) {
        for(int i = 0; i < readArray.length; i++) {
            readArray[i] = sortedArray[i];
        }

        try {
            System.out.println(outFile);
            bw = new BufferedWriter(new FileWriter(outFile));

            for(int j = 0; j < readArray.length; j++) {
                bw.write(readArray[j] + "\n");
            }
            bw.close();
        } catch(IOException e) {
            e.printStackTrace();
            System.err.println("IOException!bleee");
            System.exit(0);
        }
    }
}

class Buffer {
    public static final int THRDS_TO_START_MERGE = 2;
    ArrayList<String[]> theBuffer = new ArrayList<String[]>();
    private int threadCnt;
    private int wordsCnt;

    public Buffer(int tCnt, int wCnt) {
        threadCnt = tCnt;
        wordsCnt = wCnt;
    }

    public synchronized void add(String[] element) {
        theBuffer.add(element);
        
        if(theBuffer.size() == 2) {
            theBuffer.add(0, new SortSlave().merger(theBuffer.get(0), 
                                                    theBuffer.get(1)));
            theBuffer.remove(1);
        }

        if(theBuffer.get(0).length == wordsCnt) {
            new ioHandler().fillAndWrite(theBuffer.get(0));
        }
        
    }
}
