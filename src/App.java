import java.io.*;
import java.util.*;
/*
 * 使用方法：将单词本放入text中，每个单词占一行，每个单词用空格隔开
 * 输入单词本名称时，不需要输入.txt后缀
 * cd bin
 * java Gui
 */

public class App {
    public static void main(String[] args) {
        while (true){
            WordsCollection wordsCollection = new WordsCollection();
            wordsCollection.getWordsCollection();
            wordsCollection.run();
            System.out.println("所有单词已背过");
        }
        
    }
    

}
class Words{
    ArrayList<Integer> randomlist;
    ArrayList<String[]> words;
    public static ArrayList<String[]> readToFile(String path){
        path = "../text/" + path;
        ArrayList<String[]> list = new ArrayList<String[]>();
        try(BufferedReader br = new BufferedReader(new FileReader(path))){
            String line;
            while((line = br.readLine()) != null){
                String[] arr = line.split("\\s+");
                list.add(arr);
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        return list;
    }
    public static String[] getAllTxtFileNames() {
        // 1. 获取当前目录的路径
        String currentDirectoryPath = "../text/"; // "." 代表当前目录
        File currentDirectory = new File(currentDirectoryPath);

        // 2. 创建一个文件名过滤器
        FilenameFilter txtFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".txt");
            }
        };

        // 3. 使用过滤器列出文件
        String[] txtFileNames = currentDirectory.list(txtFilter);

        // 如果目录不存在或发生I/O错误，list()可能返回null
        if (txtFileNames == null) {
            return new String[0]; // 返回一个空数组以避免NullPointerException
        }

        return txtFileNames;
    }
    public static ArrayList<Integer> randomListGenerate(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("参数 n 不能为负数");
        }

        // 1. 创建一个包含 0 到 n-1 的列表
        ArrayList<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            numbers.add(i);
        }

        // 2. 使用 Collections.shuffle() 方法打乱列表顺序
        Collections.shuffle(numbers);
        return numbers;
    }
    public Words(ArrayList<String[]> words, ArrayList<Integer> list){
        this.words = words;
        this.randomlist = list;
    }
    public void run(){
        Scanner sc = new Scanner(System.in);
        for(int i = 0; i < randomlist.size(); i++){
            int index = randomlist.get(i);
            String[] word = words.get(index);
            System.out.println("请输入对应的单词，其释义为 "+ word[1]);
            String input = sc.nextLine().trim();
            if(input.equals("exit")){
                System.out.println("已退出");
                System.exit(0);
            }
            else if(input.equals(word[0])){
                System.out.println("恭喜你，回答正确");
            }else{
                System.out.println("很遗憾，回答错误，正确答案为 "+ word[0]);
                i-=1;
            }

            
        }
    }

}



class WordsCollection{
    ArrayList<Words> wordsCollection;
    public WordsCollection(){
        wordsCollection = new ArrayList<Words>();
    }
    public int size(){
        return wordsCollection.size();
    }
    public WordsCollection(ArrayList<Words> wordsCollection){
        this.wordsCollection = wordsCollection;
    }
    public Words getWords(int index){
        return wordsCollection.get(index);
    }
    public void addWords(Words words){
        wordsCollection.add(words);
    }
    public void getWordsCollection(){
        Scanner sc = new Scanner(System.in);
        System.out.println("请输入要学习的单词本的文件名");
        String ans = sc.nextLine().trim();
        if(ans.equals("exit")){
            System.out.println("已退出");
            System.exit(0);

        } 
        else if(ans.equals("all")){
            String[] fileNames = Words.getAllTxtFileNames();
            for(String fileName : fileNames){
                ArrayList<String[]> words = Words.readToFile(fileName);
                ArrayList<Integer> randomlist = Words.randomListGenerate(words.size());
                Words w = new Words(words, randomlist);
                addWords(w);
            }
        }
        else {
            String fileName = ans + ".txt";
            ArrayList<String[]> words = Words.readToFile(fileName);
            ArrayList<Integer> randomlist = Words.randomListGenerate(words.size());
            Words w = new Words(words, randomlist);
            addWords(w);
        }
    }
    public void run(){
        for(int i = 0; i < wordsCollection.size(); i++){
            Words w = getWords(i);
            w.run();
        }
    }
}   


