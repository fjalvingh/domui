function googleOnSignin(gu) {
    var user = gu.getBasicProfile();

    var obj = {
        name: user.getName(),
        image: user.getImageUrl(),
        email: user.getEmail(),
        token: gu.getAuthResponse().id_token
    };
    WebUI.scall("_1", "GOOGLELOGIN", obj);
}
