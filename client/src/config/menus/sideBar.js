import master from './menu/master';
import order from './menu/order';
import supplier from './menu/supplier';
import customerOrder from './menu/customerOrder';
import production from './menu/production';
import logistics from './menu/logistics';
import stock from './menu/stock';

const sideBar = [
    master,
    logistics,
    customerOrder,
    production,
    stock,
    order,
    supplier
];

export default sideBar;
