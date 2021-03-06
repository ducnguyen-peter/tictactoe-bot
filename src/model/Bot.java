package model;

import java.util.ArrayList;
import java.util.Random;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author typpo
 */
public class Bot extends Player {

//    private char name;
    private ArrayList<int[]> moves;
    private char opponent;
    private final int winScore = 100000000;
    private final int loseScore = -100000000;
    private static int index = 0;

    public Bot(char name) {
        super(name);
        moves = new ArrayList<>();
    }

    public char getOpponent() {
        return opponent;
    }

    public void setOpponent(char opponent) {
        this.opponent = opponent;
//        System.out.println("Opponent: "+ opponent);
    }

    @Override
    public boolean makeMove(int row, int col, Board board) {
        if (board.isEmptyAt(row, col)) {
            board.setValAt(row, col, this.getName());
//            moves.add(new int[]{row, col});
            return true;
        }
//        System.out.println("hehe");
        return false;
    }

    public boolean undoMove(int row, int col, Board board) {
        if (!board.isEmptyAt(row, col)) {
            board.setValAt(row, col, '.');
//            moves.add(new int[]{row, col});
            return true;
        }
//        System.out.println("hehe");
        return false;
    }

    public boolean makeMoveOpponent(int row, int col, Board board) {
        if (board.isEmptyAt(row, col)) {
            board.setValAt(row, col, this.opponent);
//            moves.add(new int[]{row, col});
//            System.out.println("Bot made move for player: " + row + ", " + col);
            return true;
        }
//        System.out.println("hehe");
        return false;
    }

    @Override
    public int[] randomChoice(Board board) {
        int row = 0, col = 0;
        Random rand = new Random();
        do {
            row = rand.nextInt(board.getSize()); //0 - n-1
            col = rand.nextInt(board.getSize());
        } while (!board.isEmptyAt(row, col)); //        System.out.println(row+" "+col);
//        System.out.println("new move: " + row + ", " + col);
        return new int[]{row, col};
    }

    public int[] bestChoice(Board board){
        int row = 0, col = 0;
        int[] move;
        double best = -100000000.0;
        if((move = searchWinMove(board))!=null){
            return move;
        }
        if((move = searchLoseMove(board))!=null){
            return move;
        }
        Node res = minimax(board, 3, -1, winScore, true);
        System.out.println("Best move(" + ++index +"): " + res.x + ", " + res.y);
        return new int[]{res.x, res.y};
    }

    private int[] searchWinMove(Board board){
        ArrayList<int[]> allPossibleMoves = generateMoves(board);
        for(int[] move : allPossibleMoves){
            Board dummyBoard = new Board();
            dummyBoard.setVal(board.getVal());
            makeMove(move[0], move[1], dummyBoard);
            if(dummyBoard.checkWin(this.name)){
                return move;
            }
        }
        return null;
    }

    private int[] searchLoseMove(Board board){
        ArrayList<int[]> allPossibleMoves = generateMoves(board);
        for(int[] move : allPossibleMoves){
            Board dummyBoard = new Board();
            dummyBoard.setVal(board.getVal());
            makeMoveOpponent(move[0], move[1], dummyBoard);
            if(dummyBoard.checkWin(this.opponent)){
                return move;
            }
        }
        return null;
    }

    public Node minimax(Board board, int depth, double alpha, double beta, boolean isMax) {
        double score = evaluateBoardForBot(board, !isMax);
        ArrayList<int[]> allPossibleMoves = generateMoves(board);
        Board dummyBoard = new Board();
        dummyBoard.setVal(board.getVal());

        if (allPossibleMoves.size()==0) {
            return new Node(score);//??i???m ??? n??t l??
        }

        if (depth == 0) {
            return new Node(score);//??i???m ??? n??t l??
        }

        Node node = new Node();

        if (isMax) {
            double maxScore = -100000000.0;//max kh???i t???o
            Node maxNode = new Node(maxScore);//node ban ?????u
            for(int move[] : allPossibleMoves){//t???t c??? n?????c ??i
                int i = move[0], j = move[1];
                makeMove(i, j, dummyBoard);//??i th??? n?????c hi???n t???i
                Node eval = minimax(dummyBoard, depth - 1, alpha, beta, false);//x??t ??i???m c???a n?????c ??i v???a xong v?? g??n ??i???m cho n??t
                eval.x = i;//g??n n?????c ??i v???a th??? cho n??t eval
                eval.y = j;
                maxNode = Node.max(maxNode, eval);//t??m n??t c?? ??i???m l???n h??n
                undoMove(i, j, dummyBoard);//r??t l???i n?????c ??i ????? ????nh gi?? n?????c ??i kh??c
                alpha = Math.max(alpha, eval.score);//c???p nh???t alpha
                if(beta<=alpha){
                    break;
                }
            }
            node = maxNode;
            return node;
        } else {
            double minScore = 100000000.0;
            Node minNode = new Node(minScore);
            for(int move[] : allPossibleMoves){
                int i = move[0], j = move[1];
                makeMoveOpponent(i, j, dummyBoard);
                Node eval = minimax(dummyBoard, depth - 1, alpha, beta, true);
                eval.x = i;
                eval.y = j;
                minNode = Node.min(minNode, eval);
                undoMove(i, j, dummyBoard);
                beta = Math.min(beta, eval.score);
                if(beta<=alpha){
                    break;
                }
            }
            node = minNode;
            return node;
        }
    }
    //c??c h??m ????nh gi??
    public double evaluateBoardForBot(Board board, boolean userTurn) {

        double opponentScore = getScore(board, true, userTurn);//??i???m c???a ?????i th??? theo l?????t userTurn
        double botScore = getScore(board, false, userTurn);//??i???m c???a bot theo l?????t c???a userTurn

        if(opponentScore == 0) opponentScore = 1.0;

        return botScore / opponentScore;//0<score<1: ?????i th??? th???ng th???; >1: bot ??ang th???ng th???

    }

    public ArrayList<int[]> generateMoves(Board boardMatrix) {//t??m nh???ng ?? tr???ng xung quanh s??t v???i v??ng ???? ????nh
        ArrayList<int[]> moveList = new ArrayList<int[]>();

        int boardSize = boardMatrix.getSize();


        // T??m nh???ng t???t c??? nh???ng ?? tr???ng nh??ng c?? ????nh XO li???n k???
        for(int i=0; i<boardSize; i++) {
            for(int j=0; j<boardSize; j++) {

                if(boardMatrix.getValAt(i,j) != '.') continue;

                if(i > 0) {
                    if(j > 0) {
                        if(boardMatrix.getValAt(i-1,j-1) != '.' ||
                                boardMatrix.getValAt(i,j-1) != '.') {
                            int[] move = {i,j};
                            moveList.add(move);
                            continue;
                        }
                    }
                    if(j < boardSize-1) {
                        if(boardMatrix.getValAt(i-1,j+1) != '.' ||
                                boardMatrix.getValAt(i,j+1) != '.') {
                            int[] move = {i,j};
                            moveList.add(move);
                            continue;
                        }
                    }
                    if(boardMatrix.getValAt(i-1,j) != '.') {
                        int[] move = {i,j};
                        moveList.add(move);
                        continue;
                    }
                }
                if( i < boardSize-1) {
                    if(j > 0) {
                        if(boardMatrix.getValAt(i+1,j-1) != '.' ||
                                boardMatrix.getValAt(i,j-1) != '.') {
                            int[] move = {i,j};
                            moveList.add(move);
                            continue;
                        }
                    }
                    if(j < boardSize-1) {
                        if(boardMatrix.getValAt(i+1,j+1) != '.' ||
                                boardMatrix.getValAt(i,j+1) != '.') {
                            int[] move = {i,j};
                            moveList.add(move);
                            continue;
                        }
                    }
                    if(boardMatrix.getValAt(i+1, j) != '.') {
                        int[] move = {i,j};
                        moveList.add(move);
                        continue;
                    }
                }

            }
        }
        return moveList;
    }

    public int getScore(Board board, boolean forOpponent, boolean botTurn){
        char[][] boardMatrix = board.getVal();
        return evaluateHorizontal(boardMatrix, forOpponent, botTurn)
                + evaluateVertical(boardMatrix, forOpponent, botTurn) + evaluateDiagonal(boardMatrix,forOpponent,botTurn);
    }

    public int getConsecutiveSetScore(int count, int blocks, boolean currentTurn) {
        final int winGuarantee = 1000000;
        if(blocks == 2 && count <= 5) return 0;
        switch(count) {
            // ??n 5 -> Cho ??i???m cao nh???t
            case 5: {
                return winScore;
            }
            case 4: {
                // ??ang 4 -> Tu??? theo l?????t v?? b??? ch???n: winGuarantee, winGuarantee/4, 200
                if(currentTurn) return winGuarantee;
                else {
                    if(blocks == 0) return winGuarantee/4;
                    else return 200;
                }
            }
            case 3: {
                // ??ang 3: Block = 0
                if(blocks == 0) {
                    // N???u l?????t c???a currentTurn th?? ??n 3 + 1 = 4 (kh??ng b??? block) -> 50000 -> Kh??? n??ng th???ng cao.
                    // Ng?????c l???i kh??ng ph???i l?????t c???a currentTurn th?? kh??? n??ng b??? blocks cao
                    if(currentTurn) return 50000;
                    else return 200;
                }
                else {
                    // Block == 1 ho???c Blocks == 2
                    if(currentTurn) return 10;
                    else return 5;
                }
            }
            case 2: {
                // T????ng t??? v???i 2
                if(blocks == 0) {
                    if(currentTurn) return 7;
                    else return 5;
                }
                else {
                    return 3;
                }
            }
            case 1: {
                return 1;
            }
        }
        return winScore*2;
    }

    public int evaluateHorizontal(char[][] boardMatrix, boolean forOpponent, boolean botTurn ) {
        int consecutive = 0;
        int blocks = 2;
        int score = 0;
//        System.out.println("Bot.evaluateHorizontal forOpponent: " + forOpponent + "; Bot: " + this.name + " User: " + this.opponent);
//        for(int i = 0; i < boardMatrix.length; i++){
//            for(int j = 0; j < boardMatrix[0].length; j++){
//                System.out.print(boardMatrix[i][j] + " ");
//            }
//            System.out.println();
//        }

        for(int i=0; i<boardMatrix.length; i++) {
            for(int j=0; j<boardMatrix[0].length; j++) {
//                ......oxxxxxo
                if(boardMatrix[i][j] == (forOpponent ?  this.opponent:this.name)) {
                    //2. ?????m...
                    consecutive++;
//                    System.out.println(boardMatrix[i][j] +" ngang: "+ consecutive);
                }
                // g???p ?? tr???ng
                else if(boardMatrix[i][j] == '.') {
                    if(consecutive > 0) {
                        // Ra: ?? tr???ng ??? cu???i sau khi ?????m. Gi???m block r???i b???t ?????u t??nh ??i???m sau ???? reset l???i ban ?????u
                        blocks--;
                        score += getConsecutiveSetScore(consecutive, blocks, forOpponent == botTurn);
                        consecutive = 0;
                        blocks = 1;
                    }
                    else {
                        // 1. V??o reset l???i blocks = 1 r???i b???t ?????u ?????m
                        blocks = 1;
                    }
                }
                //g???p qu??n ?????ch khi ??ang c?? chu???i
                else if(consecutive > 0) {
                    // 2.Ra:  ?? b??? ch???n sau khi ?????m. T??nh ??i???m sau ???? reset l???i.
                    score += getConsecutiveSetScore(consecutive, blocks, forOpponent == botTurn);
                    consecutive = 0;
                    blocks = 2;
                }
                //g???p qu??n ?????ch khi ch??a c?? chu???i
                else {
                    //1. V??o: reset l???i blocks = 2 r???i b???t ?????u ?????m
                    blocks = 2;
                }
            }

            // 3. Ra: nh??ng l??c n??y ??ang ??? cu???i. N???u li??n t???c th?? v???n t??nh cho ?????n h???t d??ng
            if(consecutive > 0) {
                score += getConsecutiveSetScore(consecutive, blocks, forOpponent == botTurn);

            }
            // reset l???i ????? ti???p t???c ch???y cho d??ng ti???p theo
            consecutive = 0;
            blocks = 2;

        }
        return score;
    }
    // h??m t??nh to??n ???????ng d???c t????ng t??? nh?? ???????ng ngan
    public int evaluateVertical(char[][] boardMatrix, boolean forOpponent, boolean botTurn ) {

        int consecutive = 0;
        int blocks = 2;
        int score = 0;
//        System.out.println("Bot.evaluateVertical forOpponent: " + forOpponent);
//        for(int i = 0; i < boardMatrix.length; i++){
//            for(int j = 0; j < boardMatrix[0].length; j++){
//                System.out.print(boardMatrix[i][j] + " ");
//            }
//            System.out.println();
//        }
        for(int j=0; j<boardMatrix[0].length; j++) {
            for(int i=0; i<boardMatrix.length; i++) {
                if(boardMatrix[i][j] == (forOpponent ?  this.opponent:this.name)) {
                    consecutive++;
//                    System.out.println(boardMatrix[i][j] +" doc: "+ consecutive);
                }
                else if(boardMatrix[i][j] == '.') {
                    if(consecutive > 0) {
                        blocks--;
                        score += getConsecutiveSetScore(consecutive, blocks, forOpponent == botTurn);
                        consecutive = 0;
                        blocks = 1;
                    }
                    else {
                        blocks = 1;
                    }
                }
                else if(consecutive > 0) {
                    score += getConsecutiveSetScore(consecutive, blocks, forOpponent == botTurn);
                    consecutive = 0;
                    blocks = 2;
                }
                else {
                    blocks = 2;
                }
            }
            if(consecutive > 0) {
                score += getConsecutiveSetScore(consecutive, blocks, forOpponent == botTurn);

            }
            consecutive = 0;
            blocks = 2;

        }
        return score;
    }
    // H??m t??nh to??n 2 ???????ng ch??o t????ng t??? nh?? h??ng ngan
    public int evaluateDiagonal(char[][] boardMatrix, boolean forOpponent, boolean botTurn ) {

        int consecutive = 0;
        int blocks = 2;
        int score = 0;
//        System.out.println("Bot.evaluateDiagonal forOpponent: " + forOpponent);
//        for(int i = 0; i < boardMatrix.length; i++){
//            for(int j = 0; j < boardMatrix[0].length; j++){
//                System.out.print(boardMatrix[i][j] + " ");
//            }
//            System.out.println();
//        }
        // ???????ng ch??o /
        for (int k = 0; k <= 2 * (boardMatrix.length - 1); k++) {
            int iStart = Math.max(0, k - boardMatrix.length + 1);
            int iEnd = Math.min(boardMatrix.length - 1, k);
            for (int i = iStart; i <= iEnd; ++i) {
                int j = k - i;

                if(boardMatrix[i][j] == (forOpponent ?  this.opponent:this.name)) {
                    consecutive++;
//                    System.out.println(boardMatrix[i][j] +" cheo/ : "+ consecutive);
                }
                else if(boardMatrix[i][j] == '.') {
                    if(consecutive > 0) {
                        blocks--;
                        score += getConsecutiveSetScore(consecutive, blocks, forOpponent == botTurn);
                        consecutive = 0;
                        blocks = 1;
                    }
                    else {
                        blocks = 1;
                    }
                }
                else if(consecutive > 0) {
                    score += getConsecutiveSetScore(consecutive, blocks, forOpponent == botTurn);
                    consecutive = 0;
                    blocks = 2;
                }
                else {
                    blocks = 2;
                }

            }
            if(consecutive > 0) {
                score += getConsecutiveSetScore(consecutive, blocks, forOpponent == botTurn);

            }
            consecutive = 0;
            blocks = 2;
        }
        // ???????ng ch??o \
        for (int k = 1-boardMatrix.length; k < boardMatrix.length; k++) {
            int iStart = Math.max(0, k);
            int iEnd = Math.min(boardMatrix.length + k - 1, boardMatrix.length-1);
            for (int i = iStart; i <= iEnd; ++i) {
                int j = i - k;

                if(boardMatrix[i][j] == (forOpponent ?  this.opponent:this.name)) {
                    consecutive++;
//                    System.out.println(boardMatrix[i][j] +" cheo"+ "\\"+ ":" + consecutive);
                }
                else if(boardMatrix[i][j] == '.') {
                    if(consecutive > 0) {
                        blocks--;
                        score += getConsecutiveSetScore(consecutive, blocks, forOpponent == botTurn);
                        consecutive = 0;
                        blocks = 1;
                    }
                    else {
                        blocks = 1;
                    }
                }
                else if(consecutive > 0) {
                    score += getConsecutiveSetScore(consecutive, blocks, forOpponent == botTurn);
                    consecutive = 0;
                    blocks = 2;
                }
                else {
                    blocks = 2;
                }

            }
            if(consecutive > 0) {
                score += getConsecutiveSetScore(consecutive, blocks, forOpponent == botTurn);

            }
            consecutive = 0;
            blocks = 2;
        }
        return score;
    }
//    public int[]
}
