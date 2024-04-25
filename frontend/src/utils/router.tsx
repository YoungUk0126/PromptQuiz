import React from 'react';
import App from '../App';
import LoginPage from '../pages/login';

const RouterInfo = [
  {
    path: '/',
    element: <App />,
    children:[
        {
            index:true,
            element:<LoginPage/>,
            label:'login'
        },
    ]
  },
];

export default RouterInfo;