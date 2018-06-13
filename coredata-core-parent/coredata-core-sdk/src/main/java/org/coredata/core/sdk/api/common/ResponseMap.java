package org.coredata.core.sdk.api.common;


import java.util.HashMap;
import java.util.Map;

/**
 * 通用的RestController返回Map
 * vesion 1.0
 */
public class ResponseMap {

	public static final String HTTPCODE = "httpCode";
	public static final String MESSAGE = "message";

	public static final String BADREQUEST_400_MSG = "用户发出的请求错误";
	public static final String INTERNAL_SERVER_ERROR_500_MSG = "服务器发生错误，无法判断发出的请求是否成功";
	public static final String OK_200_MSG = "OK";

	public static final int SC_OK = 200;
	public static final int SC_BAD_REQUEST = 400;
	public static final int SC_INTERNAL_SERVER_ERROR = 500;
	/**
	 * 200 OK - [GET]： 服务器成功返回用户请求的数据，该操作是幂等的（Idempotent）。
	 * 
	 * 201 CREATED - [POST/PUT/PATCH]： 用户新建或修改数据成功。
	 * 
	 * 202 Accepted - [*]：表示一个请求已经进入后台排队（异步任务）
	 * 
	 * 204 NO CONTENT - [DELETE]： 用户删除数据成功。
	 * 
	 * 400 INVALID REQUEST - [POST/PUT/PATCH]：
	 * 用户发出的请求有错误，服务器没有进行新建或修改数据的操作，该操作是幂等的。
	 * 
	 * 401 Unauthorized - [*]： 表示用户没有权限（令牌、用户名、密码错误）。
	 * 
	 * 403 Forbidden - [*] 表示用户得到授权（与401错误相对），但是访问是被禁止的。
	 * 
	 * 404 NOT FOUND - [*]：用户发出的请求针对的是不存在的记录，服务器没有进行操作，该操作是幂等的。
	 * 
	 * 406 Not Acceptable - [GET]：用户请求的格式不可得（比如用户请求JSON格式，但是只有XML格式）。
	 * 
	 * 410 Gone -[GET]：用户请求的资源被永久删除，且不会再得到的。
	 * 
	 * 422 Unprocesable entity - [POST/PUT/PATCH] 当创建一个对象时，发生一个验证错误。
	 * 
	 * 500 INTERNAL SERVER ERROR - [*]：服务器发生错误，用户将无法判断发出的请求是否成功。
	 */

	private Map<String, Object> MAP = null;
	private Object MAP_INNER_RESULT = null;

	public Map<String, Object> getStatus() {
		return MAP;
	}

	public Object getResult() {
		return MAP_INNER_RESULT;
	}

	public ResponseMap(int httpCode, String message) {
		MAP = new HashMap<>();
		MAP.put(HTTPCODE, httpCode);
		MAP.put(MESSAGE, message);
	}

	public void setMessage(String message) {
		MAP.put(MESSAGE, message);
	}

	public void setHttpCode(Integer httpCode) {
		MAP.put(HTTPCODE, httpCode);
	}

	/**
	 * 成功的ResponseMap

	 * @return
	 * @return ResponseMap
	 */
	public static ResponseMap SuccessInstance() {
		return new ResponseMap(SC_OK, OK_200_MSG);
	}

	/**
	 * 失败的ResponseMap

	 * @return
	 * @return ResponseMap
	 */
	public static ResponseMap ErrorInstance() {
		return new ResponseMap(SC_INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_500_MSG);
	}

	/**
	 * 参数错误的ResponseMap，比如邮箱和手机为二选一的参数，而前端二者都没有传
	 * @return
	 * @return ResponseMap
	 */
	public static ResponseMap BadRequestInstance() {
		return new ResponseMap(SC_BAD_REQUEST, BADREQUEST_400_MSG);
	}


	/**
	 * 对MAP_INNER_RESULT赋值（只能有一个MAP_INNER_RESULT）
	 *
	 * @param map
	 * @return void
	 */
	public void setResult(Object map) {
		MAP_INNER_RESULT = map;
	}

	
}
