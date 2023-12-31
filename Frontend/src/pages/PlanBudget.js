import React from 'react';
import { Link } from 'react-router-dom'
import Header from "../components/common/Header";
import styles from './PlanBudget.module.css'

const Planbudget = () => {
  const reloadPage = () => {
    window.location.reload();
  }

  return (
    <div>
     <div>
      <Header/>
      <div onClick={reloadPage} className={styles.titles}>
        <Link className={styles.backLink} to='/plan'><p className={styles.toBack}>&lt;</p>
        <p className={styles.title}>뒤로가기</p></Link> 
      </div>
      </div>
    </div>
  );
};

export default Planbudget;