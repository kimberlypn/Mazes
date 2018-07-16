// Assignment 10
// Nguyen, Kimberly
// kpnguyen
// Nguyen, Thien
// tnguyen11235

import java.util.*;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

// to represent a player
class Player {
    Vertex position;
    IList<Vertex> visited; // the vertices that the player has already visited

    Player(Vertex position) {
        this.position = position;
        this.visited = new Empty<Vertex>();
    }

    // draws this player
    WorldImage drawPlayer() {
        return new RectangleImage(Maze.CELL_SIZE, Maze.CELL_SIZE,
                OutlineMode.SOLID, new Color(121, 171, 133));
    }
}

// to represent a vertex
class Vertex {
    Edge right;
    Edge down;
    Edge left;
    Edge up;
    boolean rightFlag = true; // false implies do not render
    boolean downFlag = true; // false implies do not render
    // in logical coordinates, with the origin at the top-left corner of the
    // screen
    int x;
    int y;
    // represents the adjacent vertices of this vertex
    ArrayList<Vertex> adj = new ArrayList<Vertex>();
    boolean isVisited = false; // true if this vertex has been visited

    Vertex() {
        // convenience constructor
    }

    Vertex(int x, int y) {
        this.x = x;
        this.y = y;
    }

    Vertex(Edge right, Edge down) {
        this.right = right;
        this.down = down;
    }

    Vertex(Edge right, Edge left, Edge up, Edge down) {
        this.right = right;
        this.left = left;
        this.up = up;
        this.down = down;
    }

    // is this vertex equal to the given object?
    public boolean equals(Object other) {
        if (!(other instanceof Vertex)) {
            return false;
        }
        else {
            Vertex that = (Vertex) other;
            return this.x == that.x &&
                   this.y == that.y;
        }
    }

    // produces a hashcode for this vertex
    public int hashCode() {
        return this.x * this.y * 10000;
    }

    // draws this vertex's down edge
    WorldImage drawVert() {
        return new RectangleImage(1, Maze.CELL_SIZE, OutlineMode.SOLID,
                Color.BLACK);
    }

    // draws this vertex's right edge
    WorldImage drawHoriz() {
        return new RectangleImage(Maze.CELL_SIZE, 1, OutlineMode.SOLID,
                Color.BLACK);
    }
}

// to represent an edge
class Edge implements Comparable<Edge> {
    Vertex from;
    Vertex to;
    int weight;

    Edge(Vertex from, Vertex to, int weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
    }

    Edge(int weight) {
        this.weight = weight;
    }

    Edge(Vertex from, Vertex to) {
        this.from = from;
        this.to = to;
    }

    // is this edge equal to the given object?
    public boolean equals(Object other) {
        if (!(other instanceof Edge)) {
            return false;
        }
        else {
            Edge that = (Edge) other;
            return (this.from.equals(that.from) && this.to.equals(that.to))
                    || (this.from.equals(that.to) && this.to.equals(that.from));
        }
    }

    // produces a hashcode for this edge
    public int hashCode() {
        return this.from.hashCode() * this.to.hashCode() * 10000;
    }

    // compares this edge to the given edge
    public int compareTo(Edge o) {
        return this.weight - o.weight;
    }
}

// to represent a maze
class Maze extends World {
    ArrayList<ArrayList<Vertex>> arrVert;
    ArrayList<Edge> edgesInTree;
    ArrayList<Edge> worklist;
    HashMap<Vertex, Vertex> map = new HashMap<Vertex, Vertex>();
    IList<Vertex> searchPath = new Empty<Vertex>();
    Vertex key;
    IList<Vertex> vertices;
    Player player;
    Vertex endPoint; // represents the vertex that must be reached to win
    static final int MAZE_HEIGHT = 50;
    static final int MAZE_WIDTH = 80;
    static final int CELL_SIZE = 10;

    Maze() {
        this.initMaze();
    }

    // creates a 2D array list of vertices
    public ArrayList<ArrayList<Vertex>> initVertices() {
        ArrayList<ArrayList<Vertex>> temp = new ArrayList<ArrayList<Vertex>>();
        for (int i = 0; i < MAZE_WIDTH; i++) {
            ArrayList<Vertex> arrVertex = new ArrayList<Vertex>();
            for (int j = 0; j < MAZE_HEIGHT; j++) {
                arrVertex.add(new Vertex(i, j));
            }
            temp.add(arrVertex);
        }
        return temp;
    }

    // initializes the four edges of each vertex in the given 2D array list
    // but only returns an array list of the right and down edges
    public ArrayList<Edge> initVertEdges(ArrayList<ArrayList<Vertex>> vert) {
        ArrayList<Edge> arr = new ArrayList<Edge>();
        for (int j = 0; j < vert.size(); j++) {
            for (int i = 0; i < vert.get(j).size(); i++) {
                Vertex cur = vert.get(j).get(i);
                if (cur.x < vert.size() - 1) {
                    cur.right = new Edge(cur, vert.get(j + 1).get(i));
                    arr.add(cur.right);
                }
                if (cur.y < vert.get(j).size() - 1) {
                    cur.down = new Edge(cur, vert.get(j).get(i + 1));
                    arr.add(cur.down);
                }
                if (cur.x > 0) {
                    cur.left = new Edge(cur, vert.get(j - 1).get(i));
                }
                if (cur.y > 0) {
                    cur.up = new Edge(cur, vert.get(j).get(i - 1));
                }
            }
        }
        return arr;
    }

    // EFFECT: sets all the edge booleans of the vertices to false if they are
    // in the spanning tree
    public void fixVertEdges(ArrayList<ArrayList<Vertex>> vert) {
        for (int j = 0; j < vert.size(); j++) {
            for (int i = 0; i < vert.get(j).size(); i++) {
                Vertex cur = vert.get(j).get(i);
                if (this.edgesInTree.contains(cur.right)) {
                    cur.rightFlag = false;
                }
                if (this.edgesInTree.contains(cur.down)) {
                    cur.downFlag = false;
                }
            }
        }
    }

    // EFFECT: sets the neighbors of all the vertices
    public void fixVertAdj(ArrayList<ArrayList<Vertex>> vert) {
        for (int j = 0; j < vert.size(); j++) {
            for (int i = 0; i < vert.get(j).size(); i++) {
                Vertex cur = vert.get(j).get(i);
                if (cur.x == 0 || this.worklist.contains(cur.left)) {
                    // don't add anything
                }
                else {
                    cur.adj.add(vert.get(cur.x - 1).get(cur.y));
                }
                if (cur.x == MAZE_WIDTH - 1 || this.worklist.contains(cur.right)) {
                    // don't add anything
                }
                else {
                    cur.adj.add(vert.get(cur.x + 1).get(cur.y));
                }
                if (cur.y == 0 || this.worklist.contains(cur.up)) {
                    // don't add anything
                }
                else {
                    cur.adj.add(vert.get(cur.x).get(cur.y - 1));
                }
                if (cur.y == MAZE_HEIGHT - 1 || this.worklist.contains(cur.down)) {
                    // don't add anything
                }
                else {
                    cur.adj.add(vert.get(cur.x).get(cur.y + 1));
                }
            }
        }
    }

    // EFFECT: fixes the edges of the player's current, right, and down vertices
    public void fixPlayer() {
        if (this.player.position.x == 0) {
            this.player.position.left = null;
        }
        if (this.player.position.x == MAZE_WIDTH) {
            this.player.position.right = null;
        }
        if (this.player.position.y == 0) {
            this.player.position.up = null;
        }
        if (this.player.position.y == MAZE_HEIGHT) {
            this.player.position.down = null;
        }
        else {
            this.player.position.right = new Edge(this.player.position,
                    new Vertex(this.player.position.x + 1, this.player.position.y));
            this.player.position.left =
                    new Edge(new Vertex(this.player.position.x - 1,
                            this.player.position.y), this.player.position);
            this.player.position.up =
                    new Edge(new Vertex(this.player.position.x,
                            this.player.position.y - 1), this.player.position);
            this.player.position.down = new Edge(this.player.position,
                    new Vertex(this.player.position.x, this.player.position.y + 1));
        }
    }

    // EFFECT: randomizes the weights of the edges in the worklist
    public void randomWeights(ArrayList<Edge> edges) {
        for (int i = 0; i < edges.size(); i++) {
            edges.get(i).weight = new Random().nextInt(100);
        }
    }

    // finds the representative of the given key
    Vertex find(HashMap<Vertex, Vertex> map, Vertex key) {
        Vertex val = map.get(key);
        while (!map.get(val).equals(val)) {
            val = map.get(val);
        }
        return val;
    }

    // EFFECT: updates the to's representative to the from's representative
    void union(HashMap<Vertex, Vertex> map, Vertex to, Vertex from) {
        map.put(this.find(map, to), this.find(map, from));
    }

    // creates a hash map where each vertex's representative is initialized to itself
    public HashMap<Vertex, Vertex> initReps() {
        HashMap<Vertex, Vertex> map = new HashMap<Vertex, Vertex>();
        for (int i = 0; i < this.worklist.size(); i++) {
            Vertex to = this.worklist.get(i).to;
            Vertex from = this.worklist.get(i).from;
            map.put(to, to);
            map.put(from, from);
        }
        return map;
    }

    // determines if there is a complete spanning tree
    public boolean isTreeComplete() {
        return (MAZE_HEIGHT * MAZE_WIDTH) - 1 == this.edgesInTree.size();
    }

    // EFFECT: updates the edgesInTree with edges from the worklist after they
    // have been properly represented
    public void unionFind() {
        Collections.sort(worklist);
        HashMap<Vertex, Vertex> map = this.initReps();
        int i = 0;
        while (!this.isTreeComplete() && i < this.worklist.size() - 1) {
            Vertex to = this.worklist.get(i).to;
            Vertex from = this.worklist.get(i).from;
            if (this.find(map, to).equals(this.find(map, from))) {
                i = i + 1;
            }
            else {
                this.edgesInTree.add(this.worklist.remove(i));
                this.union(map, to, from);
            }
        }
    }

    // performs search or dfs on this maze depending on whether a queue or a stack
    // is given, respectively
    public HashMap<Vertex, Vertex> search(ICollection<Vertex> list) {
        HashMap<Vertex, Vertex> cameFromEdge = new HashMap<Vertex, Vertex>();
        ICollection<Vertex> worklist = list;
        worklist.add(this.arrVert.get(0).get(0));
        while (!worklist.isEmpty()) {
            Vertex next = worklist.remove();
            if (next.isVisited) {
                // discard it
            }
            else if (next.equals(this.endPoint)) {
                return cameFromEdge;
            }
            else {
                next.isVisited = true;
                for (Vertex v : next.adj) {
                    worklist.add(v);
                    cameFromEdge.put(v, next);
                }
            }
        }
        return cameFromEdge;
    }

    // EFFECT: initializes this maze
    void initMaze() {
        this.arrVert = this.initVertices();
        edgesInTree = new ArrayList<Edge>();
        vertices = new Empty<Vertex>();
        this.worklist = this.initVertEdges(this.arrVert);
        this.randomWeights(worklist);
        this.unionFind();
        this.fixVertEdges(arrVert);
        this.fixVertAdj(this.arrVert);
        this.map = new HashMap<Vertex, Vertex>();
        this.searchPath = new Empty<Vertex>();
        this.player = new Player(this.arrVert.get(0).get(0));
        this.fixPlayer();
        this.endPoint = this.arrVert.get(MAZE_WIDTH - 1).get(MAZE_HEIGHT - 1);
    }

    // EFFECT: moves the player/changes maps/performs search or DFS according to
    // the given key
    public void onKeyEvent(String ke) {
        this.fixPlayer();
        // checks if there is a wall in the way
        boolean rightWall = this.worklist.contains(this.player.position.right) ||
                (this.player.position.x == MAZE_WIDTH - 1);
        boolean leftWall = this.worklist.contains(this.player.position.left) ||
                (this.player.position.x == 0);
        boolean topWall = this.worklist.contains(this.player.position.up) ||
                (this.player.position.y == 0);
        boolean bottomWall = this.worklist.contains(this.player.position.down) ||
                (this.player.position.y == MAZE_HEIGHT - 1);
        if (ke.equals("right") && !rightWall) {
            this.player.visited = this.player.visited.add(this.player.position);
            this.player.position = new Vertex(this.player.position.x + 1,
                    this.player.position.y);
        }
        else if (ke.equals("left") && !leftWall) {
            this.player.visited = this.player.visited.add(this.player.position);
            this.player.position = new Vertex(this.player.position.x - 1,
                    this.player.position.y);
        }
        else if (ke.equals("up") && !topWall) {
            this.player.visited = this.player.visited.add(this.player.position);
            this.player.position = new Vertex(this.player.position.x,
                    this.player.position.y - 1);
        }
        else if (ke.equals("down") && !bottomWall) {
            this.player.visited = this.player.visited.add(this.player.position);
            this.player.position = new Vertex(this.player.position.x,
                    this.player.position.y + 1);
        }
        // to restart the game
        else if (ke.equals("r")) {
            this.initMaze();
        }
        // to perform bfs
        else if (ke.equals("b")) {
            this.initMaze();
            this.searchPath = new Empty<Vertex>();
            this.map = this.search(new Queue<Vertex>());
            this.key = this.arrVert.get(0).get(0);
        }
        // to perform dfs
        else if (ke.equals("d")) {
            this.initMaze();
            this.searchPath = new Empty<Vertex>();
            this.map = this.search(new Stack<Vertex>());
            this.key = this.arrVert.get(0).get(0);
        }
    }

    // EFFECT: animates the bfs/dfs search
    public void onTick() {
        if (this.map.isEmpty()) {
            // don't do anything
        }
        else {
            this.searchPath = this.searchPath.add(key);
            key = this.map.get(key);
        }
    }

    // draws this maze
    public WorldScene makeScene() {
        WorldScene bg = new WorldScene(Maze.MAZE_WIDTH * Maze.CELL_SIZE,
                Maze.MAZE_HEIGHT * Maze.CELL_SIZE);
        Utils util = new Utils();
        IList<Vertex> vert = util.flatten2D(arrVert);
        IListIterator<Vertex> iter = new IListIterator<Vertex>(vert);
        WorldImage player = this.player.drawPlayer();
        IListIterator<Vertex> playerIter =
                new IListIterator<Vertex>(this.player.visited);
        WorldImage endPoint = new RectangleImage(CELL_SIZE, CELL_SIZE,
                OutlineMode.SOLID, new Color(227, 161, 161));
        IListIterator<Vertex> pathIter = new IListIterator<Vertex>(this.searchPath);
        // draws the endpoint
        bg.placeImageXY(endPoint, (this.endPoint.x + 1) * CELL_SIZE -
                CELL_SIZE / 2, (this.endPoint.y + 1) * CELL_SIZE - CELL_SIZE / 2);
        // draws the player's path
        while (playerIter.hasNext()) {
            Vertex cur = playerIter.next();
            Color col = new Color(121, 171, 133);
            WorldImage path = new RectangleImage(CELL_SIZE, CELL_SIZE,
                    OutlineMode.SOLID, col.brighter());
            bg.placeImageXY(path, (cur.x + 1) * CELL_SIZE - CELL_SIZE / 2,
                    (cur.y + 1) * CELL_SIZE - CELL_SIZE / 2);
        }
        // draws the search
        if (this.map.isEmpty()) {
            // don't draw anything
        }
        else {
            WorldImage path = new RectangleImage(CELL_SIZE, CELL_SIZE,
                    OutlineMode.SOLID, new Color(109, 171, 191));
            bg.placeImageXY(path, (key.x + 1) * CELL_SIZE - CELL_SIZE / 2,
                    (key.y + 1) * CELL_SIZE - CELL_SIZE / 2);
        }
        // draws the search's path
        while (pathIter.hasNext()) {
            Vertex cur = pathIter.next();
            Color col = new Color(109, 171, 191);
            WorldImage searchPath = new RectangleImage(CELL_SIZE, CELL_SIZE,
                    OutlineMode.SOLID, col.brighter());
            bg.placeImageXY(searchPath, (cur.x + 1) * CELL_SIZE - CELL_SIZE / 2,
                    (cur.y + 1) * CELL_SIZE - CELL_SIZE / 2);
        }
        // draws the walls
        while (iter.hasNext()) {
            Vertex cur = iter.next();
            if (cur.rightFlag) {
                bg.placeImageXY(cur.drawVert(), (cur.x + 1) * CELL_SIZE,
                        (cur.y + 1) * CELL_SIZE - CELL_SIZE / 2);
            }
            if (cur.downFlag) {
                bg.placeImageXY(cur.drawHoriz(), (cur.x + 1) * CELL_SIZE -
                        CELL_SIZE / 2, (cur.y + 1) * CELL_SIZE );
            }
        }
        // draws the player at the starting position
        bg.placeImageXY(player, (this.player.position.x + 1) * CELL_SIZE -
                CELL_SIZE / 2, (this.player.position.y + 1) * CELL_SIZE -
                CELL_SIZE / 2);
        return bg;
    }

    // displays the final image once the player has reached the end point
    public WorldEnd worldEnds() {
        WorldScene bg = this.makeScene();
        WorldImage win = new TextImage("YOU WON!", MAZE_HEIGHT * CELL_SIZE / 4,
                Color.BLACK);
        if (this.player.position.equals(this.endPoint)) {
            bg.placeImageXY(win, MAZE_WIDTH * CELL_SIZE / 2,
                    MAZE_HEIGHT * CELL_SIZE / 2);
            return new WorldEnd(true, bg);
        }
        else {
            return new WorldEnd(false, this.makeScene());
        }
    }
}

// to represent an iterator for lists
class IListIterator<T> implements Iterator<T> {
    IList<T> items;

    IListIterator(IList<T> items) {
        this.items = items;
    }

    // does the list have at least one more item?
    public boolean hasNext() {
        return this.items.isCons();
    }

    // gets the next item in the list
    // EFFECT: advances the iterator to the subsequent value
    public T next() {
        if (!this.hasNext()) {
            throw new IllegalArgumentException();
        }
        Cons<T> itemsAsCons = this.items.asCons();
        T answer = itemsAsCons.first;
        this.items = itemsAsCons.rest;
        return answer;
    }

    // EFFECT: removes the item just returned by next()
    public void remove() {
        throw new UnsupportedOperationException();
    }
}

// to represent utilities
class Utils {
    // converts the given array list into an IList
    <T> IList<T> flatten(ArrayList<T> alist) {
        IList<T> temp = new Empty<T>();
        for (int i = 0; i < alist.size(); i++) {
            temp = temp.add(alist.get(i));
        }
        return temp;
    }

    // converts the given 2D array list into an IList
    <T> IList<T> flatten2D(ArrayList<ArrayList<T>> alist) {
        Utils util = new Utils();
        IList<T> temp = new Empty<T>();
        for (int row = 0; row < alist.size(); row++) {
            temp = temp.append(util.flatten(alist.get(row)));
        }
        return temp;
    }

    // flattens a 2D array list into a 1D array list
    <T> ArrayList<T> flattenTo1D(ArrayList<ArrayList<T>> alist) {
        ArrayList<T> temp = new ArrayList<T>();
        for (int row = 0; row < alist.size(); row++) {
            for (int col = 0; col < alist.get(row).size(); col++) {
                temp.add(alist.get(row).get(col));
            }
        }
        return temp;
    }

    // checks if the given array list contains the given item
    <T> boolean arrContains(ArrayList<T> arr, T item) {
        for (int i = 0; i < arr.size(); i++) {
            T cur = arr.get(i);
            if (cur.equals(item)) {
                return true;
            }
        }
        return false;
    }
}

// to represent a list of T
interface IList<T> extends Iterable<T> {
    // calculates the size of this list
    int size();

    // adds the given item to this list
    IList<T> add(T given);

    // appends the given list onto this list
    IList<T> append(IList<T> given);

    // casts this list as a cons
    Cons<T> asCons();

    // is this a cons?
    boolean isCons();

    // creates an iterator for this list
    Iterator<T> iterator();
}

// to represent an empty list of T
class Empty<T> implements IList<T> {
    // calculates the size of this list
    public int size() {
        return 0;
    }

    // adds the given item to this list
    public IList<T> add(T given) {
        return new Cons<T>(given, this);
    }

    // appends the given list onto this list
    public IList<T> append(IList<T> given) {
        return given;
    }

    // casts this list as a cons
    public Cons<T> asCons() {
        throw new ClassCastException();
    }

    // is this a cons?
    public boolean isCons() {
        return false;
    }

    // creates an iterator for this list
    public Iterator<T> iterator() {
        return new IListIterator<T>(this);
    }
}

// to represent a non-empty list of T
class Cons<T> implements IList<T> {
    T first;
    IList<T> rest;

    Cons(T first, IList<T> rest) {
        this.first = first;
        this.rest = rest;
    }

    // calculates the size of this list
    public int size() {
        return 1 + this.rest.size();
    }

    // adds the given item to this list
    public IList<T> add(T given) {
        return this.append(new Cons<T>(given, new Empty<T>()));
    }

    // appends the given list onto this list
    public IList<T> append(IList<T> given) {
        return new Cons<T>(this.first, this.rest.append(given));
    }

    // casts this list as a cons
    public Cons<T> asCons() {
        return this;
    }

    // is this a cons?
    public boolean isCons() {
        return true;
    }

    // creates an iterator for this list
    public Iterator<T> iterator() {
        return new IListIterator<T>(this);
    }
}

// to represent examples and tests
class ExamplesMaze {
    Maze world;

    // to start the game
     void testGame(Tester t) {
         world = new Maze();
         world.bigBang(Maze.MAZE_WIDTH * Maze.CELL_SIZE, Maze.MAZE_HEIGHT
                 * Maze.CELL_SIZE, .01);
     }

    ArrayList<Edge> edges = new ArrayList<Edge>();
    Utils util = new Utils();
    HashMap<Vertex, Vertex> map = new HashMap<Vertex, Vertex>();
    Player p1 = new Player(new Vertex(0, 0));

    Vertex v00 = new Vertex(0, 0);
    Vertex v10 = new Vertex(1, 0);
    Vertex v20 = new Vertex(2, 0);
    Vertex v01 = new Vertex(0, 1);
    Vertex v11 = new Vertex(1, 1);
    Vertex v21 = new Vertex(2, 1);

    Edge v00v10 = new Edge(this.v00, this.v10, 5);
    Edge v10v20 = new Edge(this.v10, this.v20, 3);
    Edge v01v11 = new Edge(this.v01, this.v11, 10);
    Edge v11v21 = new Edge(this.v11, this.v21, 12);
    Edge v00v01 = new Edge(this.v00, this.v01, 15);
    Edge v10v11 = new Edge(this.v10, this.v11, 20);
    Edge v20v21 = new Edge(this.v20, this.v21, 4);

    ArrayList<Edge> arrEdge1 = new ArrayList<Edge>();

    ArrayList<ArrayList<Vertex>> arrVertex = new ArrayList<ArrayList<Vertex>>();

    IList<Edge> mtEdge = new Empty<Edge>();
    IList<Edge> loe1 = new Cons<Edge>(this.v00v10, new Cons<Edge>(this.v10v20,
            this.mtEdge));

    // to initialize the data for tests
    void initData() {
        this.world = new Maze();

        this.v00 = new Vertex(0, 0);
        this.v10 = new Vertex(1, 0);
        this.v20 = new Vertex(2, 0);
        this.v01 = new Vertex(0, 1);
        this.v11 = new Vertex(1, 1);
        this.v21 = new Vertex(2, 1);

        this.map = new HashMap<Vertex, Vertex>();
        map.put(this.v00, this.v00);
        map.put(this.v10, this.v00);
        map.put(this.v20, this.v10);
        map.put(this.v11, this.v21);
        map.put(this.v21, this.v21);

        this.arrVertex = new ArrayList<ArrayList<Vertex>>();
        arrVertex.add(0, new ArrayList<Vertex>());
        arrVertex.add(1, new ArrayList<Vertex>());
        arrVertex.add(2, new ArrayList<Vertex>());
        arrVertex.get(0).add(this.v00);
        arrVertex.get(0).add(this.v01);
        arrVertex.get(1).add(this.v10);
        arrVertex.get(1).add(this.v11);
        arrVertex.get(2).add(this.v20);
        arrVertex.get(2).add(this.v21);

        this.v00v10 = new Edge(this.v00, this.v10, 5);
        this.v10v20 = new Edge(this.v10, this.v20, 3);
        this.v01v11 = new Edge(this.v01, this.v11, 10);
        this.v11v21 = new Edge(this.v11, this.v21, 12);
        this.v00v01 = new Edge(this.v00, this.v01, 15);
        this.v10v11 = new Edge(this.v10, this.v11, 20);
        this.v20v21 = new Edge(this.v20, this.v21, 4);

        this.arrEdge1 = new ArrayList<Edge>();
    }

    // to test the method drawPlayer
    void testDrawPlayer(Tester t) {
        t.checkExpect(this.p1.drawPlayer(),
                new RectangleImage(Maze.CELL_SIZE, Maze.CELL_SIZE,
                OutlineMode.SOLID, new Color(121, 171, 133)));
    }

    // to test the method equals for vertex
    void testVertexEqual(Tester t) {
        t.checkExpect(this.v00.equals(this.v00), true);
        t.checkExpect(this.v11.equals(this.v11), true);
        t.checkExpect(this.v00.equals(this.v01), false);
        t.checkExpect(this.v11.equals(this.v20), false);
    }

    // to test the method hashCode for vertex
    void testVertexHashCode(Tester t) {
        t.checkExpect(this.v00.hashCode(), 0);
        t.checkExpect(this.v11.hashCode(), 10000);
    }

    // to test the method drawVert
    void testDrawVert(Tester t) {
        t.checkExpect(this.v00.drawVert(), new RectangleImage(1,
                Maze.CELL_SIZE, OutlineMode.SOLID, Color.BLACK));
        t.checkExpect(this.v10.drawVert(), new RectangleImage(1,
                Maze.CELL_SIZE, OutlineMode.SOLID, Color.BLACK));
    }

    // to test the method drawHoriz
    void testDrawHoriz(Tester t) {
        t.checkExpect(this.v00.drawHoriz(), new RectangleImage(Maze.CELL_SIZE,
                1, OutlineMode.SOLID, Color.BLACK));
        t.checkExpect(this.v10.drawHoriz(), new RectangleImage(Maze.CELL_SIZE,
                1, OutlineMode.SOLID, Color.BLACK));
    }

    // to test the method equals for edge
    void testEdgeEqual(Tester t) {
        t.checkExpect(this.v00v01.equals(this.v00v01), true);
        t.checkExpect(this.v00v10.equals(this.v00v01), false);
    }

    // to test the method hashCode for edge
    void testEdgeHashCode(Tester t) {
        t.checkExpect(this.v00v01.hashCode(), 0);
        t.checkExpect(this.v11v21.hashCode(), -1454759936);
    }

    // to test the method compareTo for edge
    void testEdgeCompareTo(Tester t) {
        t.checkExpect(this.v00v01.compareTo(this.v01v11), 5);
        t.checkExpect(this.v00v10.compareTo(this.v20v21), 1);
    }

    // to test the method initVertices
    void testInitVertices(Tester t) {
        this.initData();
        ArrayList<ArrayList<Vertex>> arr = this.world.initVertices();
        t.checkExpect(arr.get(0).get(1), new Vertex(0, 1));
        t.checkExpect(arr.get(5).get(7), new Vertex(5, 7));
    }

    // to test the method initVertEdges
    void testInitVertEdges(Tester t) {
        this.initData();
        world.initVertEdges(this.arrVertex);
        t.checkExpect(this.v00.down, new Edge(this.v00, this.v01));
        t.checkExpect(this.v00.right, new Edge(this.v00, this.v10));
        t.checkExpect(this.v20.right, null);
    }

    // to test the method fixPlayer
    void testFixPlayer(Tester t) {
        this.initData();
        world.player = new Player(new Vertex(5, 5));
        world.fixPlayer();
        t.checkExpect(world.player.position.right, new Edge(world.player.position,
                new Vertex(6, 5)));
        t.checkExpect(world.player.position.left, new Edge(new Vertex(4, 5),
                world.player.position));
        t.checkExpect(world.player.position.up, new Edge(new Vertex(5, 4),
                world.player.position));
        t.checkExpect(world.player.position.down, new Edge(world.player.position,
                new Vertex(5, 6)));
    }

    // to test the method randomWeights
    void testRandomWeights(Tester t) {
        this.initData();
        world.initMaze();
        t.checkRange(world.worklist.get(0).weight, 0, 99);
        t.checkRange(world.worklist.get(5).weight, 0, 99);
    }

    // to test the method find
    void testFind(Tester t) {
        this.initData();
        t.checkExpect(this.world.find(map, this.v20), this.v00);
        t.checkExpect(this.world.find(map, this.v00), this.v00);
        t.checkExpect(this.world.find(map,  this.v11), this.v21);
    }

    // to test the method union
    void testUnion(Tester t) {
        this.initData();
        this.world.union(map, this.v11, this.v20);
        t.checkExpect(map.get(this.v21), this.v00);
    }

    // to test the method initReps
    void testInitReps(Tester t) {
        this.initData();
        ArrayList<Edge> arr = new ArrayList<Edge>();
        arr.add(this.v00v01);
        arr.add(this.v01v11);
        arr.add(this.v11v21);
        world.worklist = arr;
        HashMap<Vertex, Vertex> map = world.initReps();
        t.checkExpect(map.get(world.worklist.get(0).from), this.v00);
        t.checkExpect(map.get(world.worklist.get(0).to), this.v01);
    }

    // to test the method isTreeComplete
    void testIsTreeComplete(Tester t) {
        this.initData();
        world.edgesInTree = new ArrayList<Edge>();
        t.checkExpect(world.isTreeComplete(), false);
        world.initMaze();
        t.checkExpect(world.isTreeComplete(), true);
    }

    // to test the method unionFind
    void testUnionFind(Tester t) {
        this.initData();
        world.initMaze();
        t.checkExpect(this.world.edgesInTree.size(), Maze.MAZE_HEIGHT *
                Maze.MAZE_WIDTH - 1);
        t.checkExpect(this.world.worklist.size() < this.world.edgesInTree.size(),
                true);
        t.checkExpect(this.world.worklist.contains(this.world.edgesInTree.get(0)),
                false);
    }

    // to test the method size
    void testSize(Tester t) {
        t.checkExpect(this.mtEdge.size(), 0);
        t.checkExpect(this.loe1.size(), 2);
    }

    // to test the method add
    void testAdd(Tester t) {
        t.checkExpect(this.mtEdge.add(this.v00v01),
                new Cons<Edge>(this.v00v01, this.mtEdge));
        t.checkExpect(this.loe1.add(this.v01v11), new Cons<Edge>(this.v00v10,
                new Cons<Edge>(this.v10v20, new Cons<Edge>(this.v01v11,
                        this.mtEdge))));
    }

    // to test the method append
    void testAppend(Tester t) {
        t.checkExpect(this.mtEdge.append(this.loe1), this.loe1);
        t.checkExpect(this.loe1.append(this.mtEdge), this.loe1);
        t.checkExpect(this.loe1.append(this.loe1), new Cons<Edge>(this.v00v10,
                new Cons<Edge>(this.v10v20, new Cons<Edge>(this.v00v10,
                        new Cons<Edge>(this.v10v20, this.mtEdge)))));
    }

    // to test the method asCons
    void testAsCons(Tester t) {
        t.checkException(new ClassCastException(), this.mtEdge, "asCons");
        t.checkExpect(this.loe1.asCons(), this.loe1);
    }

    // to test the method isCons
    void testIsCons(Tester t) {
        t.checkExpect(this.mtEdge.isCons(), false);
        t.checkExpect(this.loe1.isCons(), true);
    }

    // to test the method iterator
    void testIterator(Tester t) {
        t.checkExpect(this.mtEdge.iterator(), new IListIterator<Edge>(this.mtEdge));
        t.checkExpect(this.loe1.iterator(), new IListIterator<Edge>(this.loe1));
    }
}
