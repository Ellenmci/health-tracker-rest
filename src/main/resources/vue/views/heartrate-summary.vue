<template id="heartrate-summary">
  <app-layout>

    <div class="card bg-light mb-3">
      <div class="card-header">
        <div class="row">
          <div class="col-6">Heart Rates</div>
          <div class="col" align="right">
            <button class="btn btn-info btn-simple btn-link" @click="hideForm = !hideForm">
              <i class="fa fa-plus"></i>
            </button>
          </div>
        </div>
      </div>

      <div class="card-body" :class="{ 'd-none': hideForm }">
        <form>
          <div class="input-group mb-3">
            <div class="input-group-prepend">
              <span class="input-group-text">BPM</span>
            </div>
            <input type="number" class="form-control" v-model="formData.bpm" />
          </div>

          <div class="input-group mb-3">
            <div class="input-group-prepend">
              <span class="input-group-text">Measured At</span>
            </div>
            <input type="text" class="form-control" v-model="formData.measuredAt" placeholder="YYYY-MM-DDTHH:mm" />
          </div>
        </form>

        <button class="btn btn-info btn-simple btn-link" @click="addHeartRate">Add Heart Rate</button>
      </div>
    </div>

    <div class="list-group list-group-flush">
      <div class="list-group-item"
           v-for="(h,index) in rates" :key="index">
        {{ h.measuredAt }} — {{ h.bpm }} bpm
      </div>
    </div>

  </app-layout>
</template>

<script>
app.component("heartrate-summary", {
  template: "#heartrate-summary",
  data: () => ({
    rates: [],
    formData: [],
    hideForm: true
  }),

  created() {
    const id = this.$javalin.pathParams["user-id"];
    axios.get(`/api/users/${id}/heartrates`)
        .then(res => this.rates = res.data);
  },

  methods: {
    addHeartRate() {
      const id = this.$javalin.pathParams["user-id"];
      axios.post(`/api/users/${id}/heartrates`, {
        bpm: Number(this.formData.bpm),
        measuredAt: this.formData.measuredAt,
        userId: id
      })
          .then(res => {
            this.rates.push(res.data);
            this.hideForm = true;
          });
    }
  }
});
</script>
yle>