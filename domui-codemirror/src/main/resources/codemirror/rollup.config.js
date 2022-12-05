import {nodeResolve} from "@rollup/plugin-node-resolve"
export default {
	input: "./cm.js",
	output: {
		file: "./cm.bundle.js",
		format: "iife"
	},
	plugins: [nodeResolve()]
}
