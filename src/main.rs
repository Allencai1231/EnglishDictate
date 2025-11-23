use std::{fs, io::{self, BufRead}, process, collections::HashMap};
use rand::seq::SliceRandom;

fn main() {
    let stdin = io::stdin();
    loop {
        let mut paths: Vec<String> = match fs::read_dir("./text") {
            Ok(entries) => entries.filter_map(|e| e.ok())
                .map(|e| e.file_name().into_string().unwrap())
                .filter(|n| n.ends_with(".txt"))
                .collect(),
            Err(_) => Vec::new(),
        };
        paths.sort(); 

        println!("欢迎来到单词拼写项目");
        println!("--------------------------------");
        for (i, name) in paths.iter().enumerate() {
            println!("{}. {}", i + 1, name);
        }
        println!("--------------------------------");

        println!("请输入要学习的单词本的文件名");
        let mut input = String::new();
        stdin.read_line(&mut input).unwrap();
        let parts: Vec<&str> = input.trim().split_whitespace().collect();

        if parts.is_empty() { continue; }
        if parts[0] == "exit" {
            println!("已退出");
            process::exit(0);
        }

        let targets = if parts[0] == "all" {
            paths.clone()
        } else {
            parts.iter().map(|s| format!("{}.txt", s)).collect()
        };

        for name in targets {
            run_word_book(&name);
        }
    }
}

fn run_word_book(name: &str) {
    let path = format!("./text/{}", name);
    let file = match fs::File::open(&path) {
        Ok(f) => f,
        Err(e) => { eprintln!("无法读取文件: {}", e); return; }
    };

    let words: Vec<(String, String)> = io::BufReader::new(file).lines()
        .filter_map(|l| l.ok())
        .filter_map(|line| {
            let p: Vec<&str> = line.split(':').map(|s| s.trim()).collect();
            if p.len() >= 2 { Some((p[0].to_string(), p[1].to_string())) } else { None }
        })
        .collect();

    let checklist: HashMap<String,String> = words.iter().cloned().collect(); 

    let mut indices: Vec<usize> = (0..words.len()).collect();
    let mut rng = rand::thread_rng();
    indices.shuffle(&mut rng);

    let mut errors: HashMap<String, i32> = HashMap::new();
    let stdin = io::stdin();
    let mut i = 0;

    while i < indices.len() {
        let word_data = &words[indices[i]];
        let (key, def) = (&word_data.0, &word_data.1);

        println!("{}", name);
        println!("第{}/{}个单词", i + 1, indices.len());
        println!("请输入对应的单词，其释义为 {}", def);

        let mut ans = String::new();
        stdin.read_line(&mut ans).unwrap();
        let ans = ans.trim();

        if ans == "exit" {
            println!("已退出");
            process::exit(0);
        } else if ans == key {
            println!("恭喜你，回答正确");
            i += 1;
        } else {
            println!("很遗憾，回答错误,提示: {}", key);
            *errors.entry(key.clone()).or_insert(0) += 1;
        }
    }

    println!("该单词本已经学习完毕,以下是单词错题整理");
    println!("---------------------------");
    let mut error_list: Vec<_> = errors.iter().collect();
    error_list.sort_by(|a, b| b.1.cmp(a.1));
    
    for (k, v) in error_list {
        println!("单词: {}  释义: {}  错误次数: {}", k, checklist.get(k).unwrap(), v);
    }
}