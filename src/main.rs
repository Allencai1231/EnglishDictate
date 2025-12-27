use std::{collections::HashMap, fs, io, process};

fn main() {
    let stdin = io::stdin();
    loop {
        let mut paths: Vec<_> = fs::read_dir("./text").map_or(vec![], |entries| {
            entries.filter_map(|e| e.ok()).map(|e| e.file_name().to_string_lossy().into_owned())
                .filter(|n| n.ends_with(".txt")).collect()
        });
        paths.sort();

        println!("欢迎来到单词拼写项目\n--------------------------------");
        for (i, name) in paths.iter().enumerate() { println!("{}. {}", i + 1, name); }
        println!("--------------------------------\n请输入要学习的单词本的文件数字");

        let mut input = String::new();
        if stdin.read_line(&mut input).is_err() || input.trim() == "exit" { process::exit(0) }
        let parts: Vec<_> = input.split_whitespace().collect();
        if parts.is_empty() { continue }

        let targets: Vec<_> = if parts[0] == "all" { paths.clone() } else {
            parts.iter().filter_map(|s| s.parse::<usize>().ok().and_then(|i| paths.get(i - 1).cloned())).collect()
        };
        for name in targets { run_word_book(&name); }
    }
}

fn run_word_book(name: &str) {
    let content = fs::read_to_string(format!("./text/{}", name)).unwrap_or_else(|e| { eprintln!("无法读取文件: {}", e); String::new() });
    let initial_words: Vec<_> = content.lines().filter_map(|l| l.split_once(':'))
        .map(|(k, v)| (k.trim().to_string(), v.trim().to_string())).collect();
    if initial_words.is_empty() { return }

    let mut global_errors = HashMap::new();
    let mut cur_set = initial_words.clone();
    // 初始排序：按首字母小写
    cur_set.sort_by_key(|(k, _)| k.to_lowercase().chars().next());

    let stdin = io::stdin();
    for round in 1.. {
        if round > 1 {
            println!("\n开始第 {} 轮错词复习，当前待强化单词数: {}", round, cur_set.len());
            cur_set.sort_by_key(|(k, _)| std::cmp::Reverse(global_errors.get(k).copied().unwrap_or(0)));
        }

        let mut missed = vec![];
        for (i, (key, def)) in cur_set.iter().enumerate() {
            println!("\n单词本: {} (第 {} 轮)\n进度: {}/{}\n释义: {}", name, round, i + 1, cur_set.len(), def);
            let mut ans = String::new();
            stdin.read_line(&mut ans).ok();
            if ans.trim() == "exit" { process::exit(0) }

            if ans.trim() == key {
                println!("✅ 正确！");
            } else {
                println!("❌ 错误！正确答案是: 【 {} 】", key);
                missed.push((key.clone(), def.clone()));
                loop {
                    *global_errors.entry(key.clone()).or_insert(0) += 1;
                    println!("请重新输入单词 [ {} ] 以纠正记忆:", key);
                    let mut retry = String::new();
                    stdin.read_line(&mut retry).ok();
                    if retry.trim() == "exit" { process::exit(0) }
                    if retry.trim() == key { println!("✅ 纠正成功！"); break }
                    println!("❌ 拼写依然有误，请注意拼写: {}", key);
                }
            }
        }
        if missed.is_empty() { break } else { cur_set = missed }
    }

    if !global_errors.is_empty() {
        println!("\n学习报告 - 错词统计\n---------------------------");
        let mut report: Vec<_> = global_errors.iter().collect();
        report.sort_by_key(|v| std::cmp::Reverse(v.1));
        let checklist: HashMap<_, _> = initial_words.into_iter().collect();
        for (k, v) in report {
            println!("单词: {:<15} 错误次数: {:<3} 释义: {}", k, v, checklist.get(k).unwrap());
        }
        println!("---------------------------");
    }
}
