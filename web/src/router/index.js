import Vue from "vue";
import VueRouter from "vue-router";

Vue.use(VueRouter);

const originalPush = VueRouter.prototype.push;

VueRouter.prototype.push = function push(location) {
  return originalPush.call(this, location).catch(err => err);
};

const routes = [
  {
    path: "/",
    name: "index",
    component: () =>
      import(/* webpackChunkName: "index" */ "../views/Index.vue")
  },
  {
    path: "/reader",
    name: "Reader",
    component: () =>
      import(/* webpackChunkName: "reader" */ "../views/Reader.vue")
  },
  {
    path: "/project",
    name: "Project",
    component: () =>
      import(/* webpackChunkName: "project" */ "../views/ProjectGenerator.vue")
  }
];

const router = new VueRouter({
  // mode: "history",
  base: process.env.BASE_URL,
  routes
});

export default router;
