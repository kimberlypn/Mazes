// Assignment 10
// Nguyen, Kimberly
// kpnguyen
// Nguyen, Thien
// tnguyen11235

import java.util.ArrayList;
import tester.Tester;

// represents a boolean-valued question over values of type T
interface IPred<T> {
    boolean apply(T t);
}

// finds the given String
class FindString implements IPred<String> {
    String s;
    
    FindString(String s) {
        this.s = s;
    }
    
    // finds the given String
    public boolean apply(String given) {
        return this.s.equals(given);
    }
}

// to represent a deque
class Deque<T> {
    Sentinel<T> header;
    
    // initializes header to a new sentinel
    Deque() {
        this.header = new Sentinel<T>();
    }
    
    // convenience constructor which takes a particular sentinel value to use
    Deque(Sentinel<T> header) {
        this.header = header;
    }
    
    // counts the number of nodes in this deque, not including the header node
    int size() {
        return this.header.sizeHelp();
    }
    
    // EFFECT: inserts the given value at the front of this deque
    void addAtHead(T given) {
        this.header.addAtHeadHelp(given);
    }
    
    // EFFECT: inserts the given value at the tail of this deque
    void addAtTail(T given) {
        this.header.addAtTailHelp(given);
    }
    
    // returns the data of the first node of this deque
    T removeFromHead() {
        return this.header.removeFromHeadHelp();
    }
    
    // returns the data of the last node of this deque
    T removeFromTail() {
        return this.header.removeFromTailHelp();
    }
    
    // returns the first node in this deque for which the given predicate
    // returns true; if not found, returns header
    ANode<T> find(IPred<T> pred) {
        return this.header.findHelp(pred);
    }
    
    // removes the given node from this deque
    void removeNode(ANode<T> given) {
        this.header.removeNodeHelp(given);
    }
    
    // does this list contain the given item?
    boolean contains(T given) {
        return this.header.next.containsHelp(given);
    }
}

// to represent a node
abstract class ANode<T> {
    ANode<T> next;
    ANode<T> prev;   
    
    // counts the number of nodes
    int count() {
        return 0;
    }
    
    // removes this node
    abstract T remove();
    
    // returns this node if it satisfies the given predicate
    abstract ANode<T> find(IPred<T> pred);
    
    // EFFECT: removes the given node if it is in this list
    abstract void removeGiven(ANode<T> given);
    
    // does this node contain the given item?
    abstract boolean containsHelp(T given);
}

// to represent a sentinel
class Sentinel<T> extends ANode<T> {
    
    // initializes next and prev to this
    Sentinel() {
        this.next = this;
        this.prev = this;
    }
    
    // counts the number of nodes in the next of this sentinel
    public int sizeHelp() {
        return this.next.count();
    }
    
    // EFFECT: inserts the given value as the next of this sentinel
    public void addAtHeadHelp(T given) {
        ANode<T> oldNext = this.next;
        this.next = new Node<T>(given, oldNext, this);
    }
    
    // EFFECT: inserts the given value as the prev of this sentinel
    public void addAtTailHelp(T given) {
        ANode<T> oldPrev = this.prev;
        this.prev = new Node<T>(given, this, oldPrev);
    }
    
    // removes the next of this sentinel
    public T removeFromHeadHelp() {
        return this.next.remove();
    }
    
    // removes the prev of this sentinel
    public T removeFromTailHelp() {
        return this.prev.remove();
    }
    
    // removes this node
    public T remove() {
        throw new RuntimeException();
    }
    
    // returns the first node in this list for which the given predicate
    // returns true; if not found, returns header
    public ANode<T> findHelp(IPred<T> pred) {
        return this.next.find(pred);
    }
    
    // returns this node if it satisfies the given predicate
    public ANode<T> find(IPred<T> pred) {
        return this;
    }
    
    // EFFECT: removes the given node from this list
    public void removeNodeHelp(ANode<T> given) {
        this.next.removeGiven(given);
    }
    
    // EFFECT: removes the given node if it is in this list
    public void removeGiven(ANode<T> given) {
        return;
    }
    
    // does this node contain the given item?
    public boolean containsHelp(T given) {
        return false;
    }
}

// to represent a node
class Node<T> extends ANode<T> {
    T data;
    
    // initializes next and prev to null
    Node(T data) {
        this.next = null;
        this.prev = null;
    }
    
    // convenience constructor that initializes data to given value
    // and updates given nodes to refer back to this node
    Node(T data, ANode<T> next, ANode<T> prev) {
        this.data = data;
        
        if (next == null || prev == null) {
            throw new IllegalArgumentException();
        }
        else {
            this.prev = prev;
            this.next = next;
            prev.next = this;
            next.prev = this;
        }
    }
    
    // counts the number of nodes
    public int count() {
        return 1 + this.next.count();
    }
    
    // returns the data of this node
    // EFFECT: fixes the links between the prev and next of this node after
    // removing this node
    public T remove() {
        this.prev.next = this.next;
        this.next.prev = this.prev;
        return this.data;
    }
    
    // returns this node if it satisfies the given predicate
    public ANode<T> find(IPred<T> pred) {
        if (pred.apply(this.data)) {
            return this;
        }
        else {
            return this.next.find(pred);
        }
    }
    
    // EFFECT: removes the given node if it is in this list
    public void removeGiven(ANode<T> given) {
        if (this.equals(given)) {
            this.prev.next = this.next;
            this.next.prev = this.prev;
        }
        else {
            this.next.removeGiven(given);
        }
    }
    
    // does this node contain the given item?
    public boolean containsHelp(T given) {
        if (this.data.equals(given)) {
            return true;
        }
        else {
            return this.next.containsHelp(given);
        }
    }
}

// to represent a mutable collection of items
interface ICollection<T> {
    // is this collection empty?
    boolean isEmpty();

    // EFFECT: adds the item to the collection
    void add(T item);

    // returns the first item of the collection
    // EFFECT: removes that first item
    T remove();
}

// to represent a stack
class Stack<T> implements ICollection<T> {
    Deque<T> contents = new Deque<T>();
    
    Stack(Deque<T> contents) {
        this.contents = contents;
    }
    
    Stack() {
        // convenience constructor
    }
    
    // is this stack empty?
    public boolean isEmpty() {
        return this.contents.size() == 0;
    }
    
    // removes and returns the head of the list
    public T remove() {
        return this.contents.removeFromHead();
    }
    
    // adds an item to the head of the list
    public void add(T item) {
        this.contents.addAtHead(item);
    }
}

// to represent a queue
class Queue<T> implements ICollection<T> {
    Deque<T> contents = new Deque<T>();

    Queue() {
        // convenience constructor
    }
    
    Queue(Deque<T> contents) {
        this.contents = contents;
    }
    
    // is this queue empty? 
    public boolean isEmpty() {
        return this.contents.size() == 0;
    }

    // removes and returns the head of the list
    public T remove() {
        return this.contents.removeFromHead();
    }
    
    // adds an item to the tail of the list
    public void add(T item) {
        this.contents.addAtTail(item);
    }
}

// to represent utilities for array lists
class ArrayUtils {
    <T> ArrayList<T> reverse(ArrayList<T> source) {
        ArrayList<T> tempAList = new ArrayList<T>();
        Stack<T> tempStack = new Stack<T>(new Deque<T>(new Sentinel<T>()));
        for (int t = 0; t < source.size(); t++) {
            tempStack.add(source.get(t));
        }
        for (int i = 0; i < source.size(); i++) {
            tempAList.add(tempStack.remove());
        }
        return tempAList;
    }
}

// to represent examples and tests
class ExamplesDeque {
    Deque<String> deque1 = new Deque<String>();
    Sentinel<String> sentinel1 = new Sentinel<String>();

    Deque<String> deque2 = new Deque<String>();
    Sentinel<String> sentinel2 = new Sentinel<String>();
    Node<String> abc = new Node<String>("abc");
    Node<String> bcd = new Node<String>("bcd");
    Node<String> cde = new Node<String>("cde");
    Node<String> def = new Node<String>("def");
    
    Deque<String> deque3 = new Deque<String>();
    Sentinel<String> sentinel3 = new Sentinel<String>();
    Node<String> zxy = new Node<String>("zxy");
    Node<String> wuv = new Node<String>("wuv");
    Node<String> trs = new Node<String>("trs");
    Node<String> qop = new Node<String>("qop");
    Node<String> nlm = new Node<String>("nlm");
    
    IPred<String> findABC = new FindString("abc");
    IPred<String> findCDE = new FindString("cde");
    IPred<String> findDEF = new FindString("def");
    IPred<String> findTRS = new FindString("trs");
    
    Stack<String> stack1 = new Stack<String>();
    Stack<String> stack2 = new Stack<String>();
    
    ArrayList<Integer> ints = new ArrayList<Integer>();
    ArrayList<Integer> intsReversed = new ArrayList<Integer>();
    
    ArrayUtils ArrayUtils = new ArrayUtils();

    // to initialize the data for tests
    void initData() {
        this.deque1 = new Deque<String>();
        this.sentinel1 = new Sentinel<String>();
        
        this.deque2 = new Deque<String>();
        this.sentinel2 = new Sentinel<String>();
        this.abc = new Node<String>("abc");
        this.bcd = new Node<String>("bcd");
        this.cde = new Node<String>("cde");
        this.def = new Node<String>("def");
        this.deque2 = new Deque<String>(this.sentinel2);
        this.abc = new Node<String>("abc", this.bcd, this.sentinel2);
        this.bcd = new Node<String>("bcd", this.cde, this.abc);
        this.cde = new Node<String>("cde", this.def, this.bcd);
        this.def = new Node<String>("def", this.sentinel2, this.cde);
        
        this.deque3 = new Deque<String>();
        this.sentinel3 = new Sentinel<String>();
        this.zxy = new Node<String>("zxy");
        this.wuv = new Node<String>("wuv");
        this.trs = new Node<String>("trs");
        this.qop = new Node<String>("qop");
        this.nlm = new Node<String>("nlm");
        this.deque3 = new Deque<String>(this.sentinel3);
        this.zxy = new Node<String>("zxy", this.wuv, this.sentinel3);
        this.wuv = new Node<String>("wuv", this.trs, this.zxy);
        this.trs = new Node<String>("trs", this.qop, this.wuv);
        this.qop = new Node<String>("qop", this.nlm, this.trs);
        this.nlm = new Node<String>("nlm", this.sentinel3, this.qop);
        
        this.stack1 = new Stack<String>(this.deque1);
        this.stack2 = new Stack<String>(this.deque2);
        
        this.ints = new ArrayList<Integer>();
        this.intsReversed = new ArrayList<Integer>();
    }
    
    // to test the method size
    void testSize(Tester t) {
        this.initData();
        t.checkExpect(this.deque1.size(), 0);
        t.checkExpect(this.deque2.size(), 4);
        t.checkExpect(this.deque3.size(), 5);
    }
    
    // to test the method addAtHead
    void testAddAtHead(Tester t) {
        this.initData();
        this.deque1.addAtHead("abc");
        this.deque2.addAtHead("123");
        this.deque3.addAtHead("kij");
        t.checkExpect(this.deque1.header.next, new Node<String>("abc",
                this.deque1.header, this.deque1.header));
        t.checkExpect(this.sentinel2.next, new Node<String>("123", 
                this.abc, this.sentinel2));
        t.checkExpect(this.abc.prev, new Node<String>("123", this.abc,
                this.sentinel2));
        t.checkExpect(this.sentinel3.next, new Node<String>("kij", 
                this.zxy, this.sentinel3));
    }
    
    // to test the method addAtTail
    void testAddAtTail(Tester t) {
        this.initData();
        this.deque1.addAtTail("abc");
        this.deque2.addAtTail("efg");
        t.checkExpect(this.deque1.header.next, new Node<String>("abc",
                this.deque1.header, this.deque1.header));
        t.checkExpect(this.deque1.header.prev, new Node<String>("abc",
                this.deque1.header, this.deque1.header));
        t.checkExpect(this.sentinel2.prev, new Node<String>("efg", 
                this.sentinel2, this.def));
    }
    
    // to test the method removeFromHead
    void testRemoveFromHead(Tester t) {
        this.initData();
        t.checkException(new RuntimeException(), this.deque1, "removeFromHead");
        t.checkExpect(this.deque2.removeFromHead(), "abc");
        t.checkExpect(this.deque3.removeFromHead(), "zxy");
    }
    
    // to test the method removeFromTail
    void testRemoveFromTail(Tester t) {
        this.initData();
        t.checkException(new RuntimeException(), this.deque1, "removeFromTail");
        t.checkExpect(this.deque2.removeFromTail(), "def");
        t.checkExpect(this.deque2.removeFromTail(), "cde");
        t.checkExpect(this.deque3.removeFromTail(), "nlm");
    }
    
    // to test the method find
    void testFind(Tester t) {
        this.initData();
        t.checkExpect(this.deque1.find(this.findABC), this.sentinel1);
        t.checkExpect(this.deque2.find(this.findABC), this.abc);
        t.checkExpect(this.deque2.find(this.findCDE), this.cde);
        t.checkExpect(this.deque2.find(this.findDEF), this.def);
        t.checkExpect(this.deque2.find(this.findTRS), this.sentinel2);
    }
    
    // to test the method removeNode
    void testRemoveNode(Tester t) {
        this.initData();
        this.deque1.removeNode(this.sentinel1);
        this.deque2.removeNode(this.cde);
        this.deque2.removeNode(this.bcd);
        this.deque1.removeNode(this.qop);
        t.checkExpect(this.deque1.header, this.sentinel1);
        t.checkExpect(this.bcd.next, this.def);
        t.checkExpect(this.abc.next, this.def);
        this.deque2.removeNode(this.abc);
        this.deque2.removeNode(this.def);
        t.checkExpect(this.sentinel2.next, this.sentinel2);
        t.checkExpect(this.sentinel2.prev, this.sentinel2);
    }
    
    // to test the method count
    void testCount(Tester t) {
        this.initData();
        t.checkExpect(this.abc.count(), 4);
        t.checkExpect(this.sentinel1.count(), 0);
        t.checkExpect(this.sentinel2.count(), 0);
        t.checkExpect(this.def.count(), 1);
    }
    
    // to test the method remove
    void testRemove(Tester t) {
        this.initData();
        t.checkException(new RuntimeException(), this.sentinel1, "remove");
        t.checkExpect(this.abc.remove(), "abc");
        t.checkExpect(this.nlm.remove(), "nlm");
        t.checkExpect(this.bcd.remove(), "bcd");
    }
    
    // to test the method find for sentinels/nodes
    void testFind4Nodes(Tester t) {
        this.initData();
        t.checkExpect(this.sentinel1.find(this.findABC), this.sentinel1);
        t.checkExpect(this.bcd.find(this.findABC), this.sentinel2);
        t.checkExpect(this.abc.find(this.findABC), this.abc);
    }
    
    // to test the method removeGiven
    void testRemoveGiven(Tester t) {
        this.initData();
        this.abc.removeGiven(this.abc);
        t.checkExpect(this.sentinel2.next, this.bcd);
        this.abc.removeGiven(this.zxy);
        t.checkExpect(this.abc.next, this.bcd);
        this.sentinel1.removeGiven(this.sentinel1);
        t.checkExpect(this.sentinel1.next, this.sentinel1);
    }
    
    // to test the method sizeHelp
    void testSizeHelp(Tester t) {
        this.initData();
        t.checkExpect(this.sentinel1.sizeHelp(), 0);
        t.checkExpect(this.sentinel2.sizeHelp(), 4);
        t.checkExpect(this.sentinel3.sizeHelp(), 5);
    }
    
    // to test the method addAtHeadHelp
    void testAddAtHeadHelp(Tester t) {
        this.initData();
        this.sentinel1.addAtHeadHelp("123");
        t.checkExpect(this.sentinel1.next, new Node<String>("123", 
                this.sentinel1, this.sentinel1));
        this.sentinel2.addAtHeadHelp("123");
        t.checkExpect(this.sentinel2.next, new Node<String>("123", this.abc,
                this.sentinel2));
    }
    
    // to test the method addAtTailHelp
    void testAddAtTailHelp(Tester t) {
        this.initData();
        this.sentinel1.addAtTailHelp("123");
        t.checkExpect(this.sentinel1.next, new Node<String>("123", 
                this.sentinel1, this.sentinel1));
        t.checkExpect(this.sentinel1.prev, new Node<String>("123", 
                this.sentinel1, this.sentinel1));
        this.sentinel2.addAtTailHelp("123");
        t.checkExpect(this.sentinel2.prev, new Node<String>("123", 
                this.sentinel2, this.def));
    }
    
    // to test the method removeFromHeadHelp
    void testRemoveFromHeadHelp(Tester t) {
        this.initData();
        this.sentinel2.removeFromHeadHelp();
        t.checkExpect(this.sentinel2.next, this.bcd);
        this.sentinel2.removeFromHeadHelp();
        t.checkExpect(this.sentinel2.next, this.cde);
    }
    
    // to test the method removeFromTailHelp
    void testRemoveFromTailHelp(Tester t) {
        this.initData();
        this.sentinel2.removeFromTailHelp();
        t.checkExpect(this.sentinel2.prev, this.cde);
        this.sentinel2.removeFromTailHelp();
        t.checkExpect(this.sentinel2.prev, this.bcd);
    }
    
    // to test the method findHelp
    void testFindHelp(Tester t) {
        this.initData();
        t.checkExpect(this.sentinel1.findHelp(this.findABC), this.sentinel1);
        t.checkExpect(this.sentinel2.findHelp(this.findABC), this.abc);
        t.checkExpect(this.sentinel2.findHelp(this.findTRS), this.sentinel2);
    }
    
    // to test the method removeNodeHelp
    void testRemoveNodeHelp(Tester t) {
        this.initData();
        this.sentinel1.removeNodeHelp(this.sentinel1);
        t.checkExpect(this.sentinel1.next, this.sentinel1);
        this.sentinel2.removeNodeHelp(this.bcd);
        t.checkExpect(this.abc.next, this.cde);
        t.checkExpect(this.cde.prev, this.abc);
    }
    
    // to test the method count for nodes
    void testCount4Nodes(Tester t) {
        this.initData();
        t.checkExpect(this.abc.count(), 4);
        t.checkExpect(this.def.count(), 1);
        t.checkExpect(this.bcd.count(), 3);
    }
    
    // to test the method add
    void testadd(Tester t) {
        this.initData();
        this.stack1.add("123");
        t.checkExpect(this.stack1.contents.header.next, new Node<String>("123", 
                this.sentinel1, this.sentinel1));
    }
    
    // to test the method reverse
    void testReverse(Tester t) {
        this.initData();
        this.ints.add(1);
        this.ints.add(2);
        this.ints.add(3);
        this.intsReversed.add(3);
        this.intsReversed.add(2);
        this.intsReversed.add(1);
        t.checkExpect(this.ints.get(0), 1);
        t.checkExpect(this.intsReversed.get(0), 3);
        t.checkExpect(ArrayUtils.reverse(this.ints), this.intsReversed);
    }
}