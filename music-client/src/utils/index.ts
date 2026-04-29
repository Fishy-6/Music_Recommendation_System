// 解析日期
export function getBirth(value) {
  if (value == null || value == "") return "";
  const date = new Date(value);
  const year = date.getFullYear();
  const month = date.getMonth() + 1 < 10 ? "0" + (date.getMonth() + 1) : date.getMonth() + 1;
  const day = date.getDate() < 10 ? "0" + date.getDate() : date.getDate();
  return year + "-" + month + "-" + day;
}

/**
 * 表格时间格式化
 */
export function formatDate(cellValue) {
  if (cellValue == null || cellValue == "") return "";
  const date = new Date(cellValue);
  const year = date.getFullYear();
  const month = date.getMonth() + 1 < 10 ? "0" + (date.getMonth() + 1) : date.getMonth() + 1;
  const day = date.getDate() < 10 ? "0" + date.getDate() : date.getDate();
  const hours = date.getHours() < 10 ? "0" + date.getHours() : date.getHours();
  const minutes = date.getMinutes() < 10 ? "0" + date.getMinutes() : date.getMinutes();
  const seconds = date.getSeconds() < 10 ? "0" + date.getSeconds() : date.getSeconds();
  return year + "-" + month + "-" + day + " " + hours + ":" + minutes + ":" + seconds;
}

// 解析歌词
// export function parseLyric(text) {
//   let lines = text.split("\n");
//   const pattern = /\[\d{2}:\d{2}.(\d{3}|\d{2})\]/g;
//   const result = [];

//   // 对于歌词格式不对的特殊处理
//   if (!/\[.+\]/.test(text)) {
//     return [[0, text]];
//   }

//   while (!pattern.test(lines[0])) {
//     lines = lines.slice(1);
//   }

//   lines[lines.length - 1].length === 0 && lines.pop();
//   for (const item of lines) {
//     const time = item.match(pattern); // 存前面的时间段
//     const value = item.replace(pattern, ""); // 存歌词
//     for (const item1 of time) {
//       const t = item1.slice(1, -1).split(":");
//       if (value !== "") {
//         result.push([parseInt(t[0], 10) * 60 + parseFloat(t[1]), value]);
//       }
//     }
//   }
//   result.sort((a, b) => a[0] - b[0]);
//   return result;
// }

export const parseLyric = (lrc: string): [number, string][] => {
  if (!lrc || typeof lrc !== 'string') {
    console.warn('歌词数据无效:', lrc);
    return [];
  }

  const lines = lrc.split('\n');
  const result: [number, string][] = [];
  
  // 正则表达式匹配时间标签
  const timeTagReg = /\[(\d+):(\d+)\.(\d+)\]/g;
  
  for (const line of lines) {
    if (!line.trim()) continue;
    
    const timeTags = [];
    let match;
    let text = line;
    
    // 提取所有时间标签
    while ((match = timeTagReg.exec(line)) !== null) {
      timeTags.push(match[0]);
      text = text.replace(match[0], '');
    }
    
    // 如果没有时间标签，跳过这一行
    if (timeTags.length === 0) continue;
    
    // 为每个时间标签创建条目
    for (const timeTag of timeTags) {
      const timeMatch = timeTag.match(/\[(\d+):(\d+)\.(\d+)\]/);
      if (!timeMatch) continue;
      
      const minutes = parseInt(timeMatch[1], 10);
      const seconds = parseInt(timeMatch[2], 10);
      const milliseconds = parseInt(timeMatch[3], 10);
      
      // 计算总时间（秒）
      const time = minutes * 60 + seconds + milliseconds / 1000;
      
      result.push([time, text.trim()]);
    }
  }
  
  // 按时间排序
  result.sort((a, b) => a[0] - b[0]);
  
  return result;
};

// 解析播放时间
export function formatSeconds(value) {
  let theTime = parseInt(value);
  let theTime1 = 0;
  let theTime2 = 0;
  if (theTime > 60) {
    theTime1 = parseInt((theTime / 60).toString()); // 分
    theTime = parseInt((theTime % 60).toString()); // 秒
    // 是否超过一个小时
    if (theTime1 > 60) {
      theTime2 = parseInt((theTime1 / 60).toString()); // 小时
      theTime1 = 60; // 分
    }
  }
  // 多少秒
  let result = "";
  if (parseInt(theTime.toString()) < 10) {
    result = "0:0" + parseInt(theTime.toString());
  } else {
    result = "0:" + parseInt(theTime.toString());
  }
  // 多少分钟时
  if (theTime1 > 0) {
    if (parseInt(theTime.toString()) < 10) {
      result = "0" + parseInt(theTime.toString());
    } else {
      result = parseInt(theTime.toString()).toString();
    }
    result = parseInt(theTime1.toString()) + ":" + result;
  }
  // 多少小时时
  if (theTime2 > 0) {
    if (parseInt(theTime.toString()) < 10) {
      result = "0" + parseInt(theTime.toString());
    } else {
      result = parseInt(theTime.toString()).toString();
    }
    result = parseInt(theTime2.toString()) + ":" + parseInt(theTime1.toString()) + ":" + result;
  }
  return result;
}
