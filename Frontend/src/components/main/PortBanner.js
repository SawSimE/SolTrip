import {React,useState} from 'react';
import { useLocation, Link, useNavigate } from "react-router-dom";
import styles from "./PortBanner.module.css";
import axios from "axios";
import location from "../../assets/location.png"
import book from "../../assets/books3.png"


const Insurance = () => {
  return (
  <div>
    <p className={styles.insuranceTitle}>여행 포트폴리오</p>
    <div style={{maxWidth:'90%', width: '90%', backgroundColor:'#316FDF',
  display:'flex',justifyContent:'space-evenly', margin: '0.5rem auto',
  borderRadius:'0.5rem'}}>
    <div style={{marginLeft:'0.4rem', padding:'1.3rem 1rem 1rem 1.2rem'}} >
    {/* <div style={{margin:'0.5rem 1rem 1rem 1rem', backgroundColor:'#316FDF',
  display:'flex',justifyContent:'space-between',
  borderRadius:'0.5rem', padding:'1.3rem 1rem 0.9rem 1.2rem',}}>
    <div style={{marginLeft:'0.4rem'}} > */}
      <p style={{opacity:'0.8',fontSize:'0.9rem',color:'#FFF',fontFamily:'preLt',
      marginTop:0,marginBottom:'0.3rem',textAlign:'start'}}>세상에 하나뿐인</p>
      <p style={{color:'white',fontSize:'1.1rem',fontFamily:'preBd',margin:'0 0 1.2rem 0',textAlign:'start'}}>나만의 여행 포트폴리오</p>
      <Link to='/portfolio' style={{color: 'white',textDecoration:'none'}}>
        <button style={{paddingTop:'0.2rem',paddingBottom:'0.2rem',marginBottom:'0.5rem', color: 'white', border:'0.02rem solid white',borderRadius:'0.4rem',
        display:'flex',margin: 0, textDecoration:'none'}} className={styles.more}>보러가기 &gt; 
        </button></Link>
    </div>
    <img style={{margin:'0', padding:'1.3rem 1rem 0.9rem 1.2rem', 
    alignItems:'center',width:'6.4rem', height:'5.8rem'}} src={book} alt="png"/>
    {/* <img style={{margin:'0.5rem 0 0.3rem 0', alignItems:'center',width:'5rem', height:'4.7rem'}} src={diary} alt="png"/> */}
  </div>
  </div>
  );
};

export default Insurance;
