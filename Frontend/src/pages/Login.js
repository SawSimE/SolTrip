import React from 'react';
import styles from "./Login.module.css";
import Header from "../components/common/Header";
import TransferOne from "../components/login/TransferOne";
import login from "./login.png";

const Login = () => {
  return (
    <div>
      <Header/><br/><br/>
      <img alt="money" src={login} className={styles.money}/>
      <p className={styles.ment1}>1원 이체를 통해 계좌를 인증하고,</p>
      <p className={styles.ment2}>쏠트립을 이용해보세요!</p>
      <TransferOne/>
    </div>
  )
}

export default Login