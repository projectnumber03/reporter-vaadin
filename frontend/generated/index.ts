/******************************************************************************
 * This file is auto-generated by Vaadin.
 * If you want to customize the entry point, you can copy this file or create
 * your own `index.ts` in your frontend directory.
 * By default, the `index.ts` file should be in `./frontend/` folder.
 *
 * NOTE:
 *     - You need to restart the dev-server after adding the new `index.ts` file.
 *       After that, all modifications to `index.ts` are recompiled automatically.
 *     - `index.js` is also supported if you don't want to use TypeScript.
 ******************************************************************************/

// import Vaadin client-router to handle client-side and server-side navigation
import {Router} from '@vaadin/router';

// import Flow module to enable navigation to Vaadin server-side views
import {Flow} from 'Frontend/generated/jar-resources/Flow.js';

const { serverSideRoutes } = new Flow({
  imports: () => import('../../target/frontend/generated-flow-imports.js')
});

const routes = [
  // for client-side, place routes below (more info https://vaadin.com/docs/v15/flow/typescript/creating-routes.html)

  // for server-side, the next magic line sends all unmatched routes:
  ...serverSideRoutes // IMPORTANT: this must be the last entry in the array
];

// Vaadin router needs an outlet in the index.html page to display views
const router = new Router(document.querySelector('#outlet'));
router.setRoutes(routes);
