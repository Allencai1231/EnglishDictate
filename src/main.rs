use std::{collections::HashMap, fs, io::{self, BufRead}, process};

fn main() {
    let stdin = io::stdin();
    loop {
        // 精简文件列表获取逻辑
        let mut paths: Vec<String> = fs::read_dir("./text").map_or(Vec::new(), |entries| {
            entries.filter_map(Result::ok)
                .map(|e| e.file_name().to_string_lossy().into_owned())
                .filter(|n| n.ends_with(".txt"))
                .collect()
        });
        paths.sort();

        println!("欢迎来到单词拼写项目\n--------------------------------");
        paths.iter().enumerate().for_each(|(i, name)| println!("{}. {}", i + 1, name));
        println!("--------------------------------\n请输入要学习的单词本的文件名");

        let mut input = String::new();
        if stdin.read_line(&mut input).is_err() { continue; }
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

    // 读取并解析文件
    let mut words: Vec<(String, String)> = io::BufReader::new(file).lines()
        .filter_map(Result::ok)
        .filter_map(|line| {
            // 使用 split_once 简化分割逻辑
            line.split_once(':').map(|(k, v)| (k.trim().to_string(), v.trim().to_string()))
        })
        .collect();

    // 精简排序逻辑：Option<T> 实现了 Ord，且 None 小于 Some，这与你之前的 match 逻辑一致。
    // 我们只需提取首字母并转为小写即可直接比较。
    words.sort_by(|a, b| {
        let get_key = |s: &str| s.chars().next().and_then(|c| c.to_lowercase().next());
        get_key(&a.0).cmp(&get_key(&b.0))
    });

    // 建立查询表
    let checklist: HashMap<String, String> = words.iter().cloned().collect();
    let mut errors: HashMap<String, i32> = HashMap::new();
    let stdin = io::stdin();
    let mut i = 0;

    // 直接使用索引遍历
    while i < words.len() {
        let (key, def) = &words[i];

        println!("{}\n第{}/{}个单词\n请输入对应的单词，其释义为 {}", name, i + 1, words.len(), def);

        let mut ans = String::new();
        stdin.read_line(&mut ans).unwrap();
        let ans = ans.trim();

        if ans == "exit" {
            println!("已退出");
            process::exit(0);
        } else if ans == key {
            println!("恭喜你，回答正确");
            i += 1; // 只有正确才进入下一个
        } else {
            println!("很遗憾，回答错误,提示: {}", key);
            *errors.entry(key.clone()).or_insert(0) += 1;
        }
    }

    println!("该单词本已经学习完毕,以下是单词错题整理\n---------------------------");
    // 将 HashMap 转为 Vec 并排序
    let mut error_list: Vec<_> = errors.iter().collect();
    error_list.sort_by(|a, b| a.0.cmp(b.0));

    for (k, v) in error_list {
        println!("单词: {}  释义: {}  错误次数: {}", k, checklist.get(k).unwrap_or(&String::new()), v);
    }
}