import React from 'react';
import { List } from 'antd';

const DailyBudget = ({ startDate, endDate }) => {
  const start = new Date(startDate);
  const end = new Date(endDate);
  const dayCount = Math.floor((end - start) / (1000 * 60 * 60 * 24)) + 1;

  const dailyInfo = [];

  for (let i = 0; i < dayCount; i++) {
    const currentDate = new Date(start);
    currentDate.setDate(currentDate.getDate() + i);

    dailyInfo.push({
      date: currentDate,
      // date: currentDate.toLocaleDateString(), // 일자를 원하는 형식으로 표시
      // 여기에서 다른 필요한 정보도 추가할 수 있습니다.
    });
  }

  console.log('startDate:', startDate);
  console.log('endDate:', endDate);

  return (
    <div>
      <h2>날짜별 예산</h2>
      <List
        itemLayout="horizontal"
        dataSource={dailyInfo}
        renderItem={(item, index) => (
          <List.Item>
            <List.Item.Meta
              title={`날짜: ${item.date.toLocaleDateString()}`}

            />
          </List.Item>
        )}
      />
    </div>
  );
};

export default DailyBudget;
