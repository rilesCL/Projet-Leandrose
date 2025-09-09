import mergeHeaders from "./mergeheader";
import BASE_URL from "../components/config/Config";

async function fetcher(input, options) {
  // your headers
  const defaultHeaders = { Authorization: localStorage.getItem('token') }
  // merge them with the headers of the options
  const headers = mergeHeaders(defaultHeaders, options.headers)
  // add the headers to the options
  try {
    input = input.replace(/^\//, '');
    const url = BASE_URL + input;
    const response = await fetch(url, {...options, headers});
    return response;
  } catch(e) {
    throw e;
  }
}
export default fetcher;